package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.BeneficiarioEndereco;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.BeneficiarioFichaDto;
import br.com.imgazin.beneficios.dto.BeneficiarioListaItemDto;
import br.com.imgazin.beneficios.exception.CpfDuplicadoException;
import br.com.imgazin.beneficios.form.BeneficiarioFiltro;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.mapper.BeneficiarioMapper;
import br.com.imgazin.beneficios.repository.BeneficiarioEnderecoRepository;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Orquestra a persistência do beneficiário. Controller nunca grava nada
 * diretamente (ver CLAUDE.md) — sempre passa por aqui.
 */
@Service
@RequiredArgsConstructor
public class BeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final BeneficiarioEnderecoRepository beneficiarioEnderecoRepository;
    private final VoluntarioRepository voluntarioRepository;
    private final RetiradaCestaRepository retiradaCestaRepository;
    private final BeneficiarioMapper mapper;
    private final BeneficiosAuditoriaService auditoriaService;

    public Beneficiario buscarPorId(Long id) {
        return beneficiarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado: " + id));
    }

    /** Busca por CPF aceitando com ou sem máscara (normaliza antes de consultar). */
    public Optional<Beneficiario> buscarPorCpf(String cpf) {
        String normalizado = CpfUtils.normalizar(cpf);
        if (normalizado == null || normalizado.length() != 11) {
            return Optional.empty();
        }
        return beneficiarioRepository.findByCpf(normalizado);
    }

    public Page<BeneficiarioListaItemDto> listar(BeneficiarioFiltro filtro, Pageable pageable) {
        return beneficiarioRepository.findAll(construirSpecification(filtro), pageable)
                .map(mapper::paraItemLista);
    }

    /**
     * Único caso de negócio que bloqueia (ver CLAUDE.md, Etapa 2): CPF já
     * cadastrado em OUTRO registro. Não lança nada se for o próprio registro
     * sendo editado.
     */
    public void verificarCpfDuplicado(BeneficiarioForm form) {
        String cpfNormalizado = CpfUtils.normalizar(form.getCpf());
        beneficiarioRepository.findByCpf(cpfNormalizado).ifPresent(existente -> {
            if (form.getId() == null || !existente.getId().equals(form.getId())) {
                throw new CpfDuplicadoException(existente.getId());
            }
        });
    }

    @Transactional
    public Beneficiario salvar(BeneficiarioForm form, List<Aviso> avisosConfirmados, String justificativa) {
        boolean novoCadastro = form.getId() == null;
        Beneficiario entidade = novoCadastro ? new Beneficiario() : buscarPorId(form.getId());

        Voluntario padrinho = null;
        if (form.getPadrinhoId() != null && !form.getPadrinhoId().isBlank()) {
            padrinho = voluntarioRepository.findById(Long.valueOf(form.getPadrinhoId().trim())).orElse(null);
        }

        mapper.aplicarNaEntidade(form, entidade, padrinho);
        aplicarStatus(form, entidade, novoCadastro);

        entidade = beneficiarioRepository.save(entidade);

        BeneficiarioEndereco endereco = beneficiarioEnderecoRepository.findByBeneficiarioId(entidade.getId())
                .orElseGet(BeneficiarioEndereco::new);
        mapper.aplicarNoEndereco(form, endereco, entidade);
        beneficiarioEnderecoRepository.save(endereco);

        registrarAuditoriaDaGravacao(entidade, novoCadastro, avisosConfirmados, justificativa);

        return entidade;
    }

    /** Regra #5 (status inicial AGUARDANDO_ANALISE) e #6 (prazo vencido não bloqueia, mas muda o status). */
    private void aplicarStatus(BeneficiarioForm form, Beneficiario entidade, boolean novoCadastro) {
        StatusBeneficiario statusInformado = parseStatus(form.getStatus());

        if (novoCadastro && statusInformado == null) {
            entidade.setStatus(StatusBeneficiario.AGUARDANDO_ANALISE);
        } else if (statusInformado != null) {
            entidade.setStatus(statusInformado);
        }

        if (entidade.getPrazoFinalBeneficio() != null && entidade.getPrazoFinalBeneficio().isBefore(LocalDate.now())) {
            entidade.setStatus(StatusBeneficiario.PRAZO_ENCERRADO);
        }
    }

    private StatusBeneficiario parseStatus(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return StatusBeneficiario.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * O cadastro não tem uma "retirada" para pendurar a confirmação (ver
     * {@code retirada_confirmacao}, que exige {@code retirada_id}) — os
     * avisos confirmados no cadastro/edição do beneficiário ficam registrados
     * na trilha de auditoria do módulo, com usuário, data/hora e a
     * justificativa informada (LGPD + exigência de confirmação explícita).
     */
    private void registrarAuditoriaDaGravacao(Beneficiario entidade, boolean novoCadastro,
                                               List<Aviso> avisosConfirmados, String justificativa) {
        AcaoAuditoria acao = novoCadastro ? AcaoAuditoria.CRIAR : AcaoAuditoria.EDITAR;
        StringBuilder detalhes = new StringBuilder("Beneficiário ")
                .append(novoCadastro ? "cadastrado" : "atualizado")
                .append(" (ID ").append(entidade.getId()).append(")");

        if (avisosConfirmados != null && !avisosConfirmados.isEmpty()) {
            String codigos = avisosConfirmados.stream().map(Aviso::getCodigo).collect(Collectors.joining(", "));
            detalhes.append(" | Avisos confirmados: ").append(codigos);
            if (justificativa != null && !justificativa.isBlank()) {
                detalhes.append(" | Justificativa: ").append(justificativa);
            }
        }

        auditoriaService.registrar(acao, "Beneficiario", entidade.getId(), detalhes.toString());
    }

    @Transactional
    public void alterarStatus(Long id, String novoStatusTexto, String motivo) {
        Beneficiario entidade = buscarPorId(id);
        StatusBeneficiario novoStatus = StatusBeneficiario.valueOf(novoStatusTexto);
        StatusBeneficiario statusAnterior = entidade.getStatus();

        entidade.setStatus(novoStatus);
        beneficiarioRepository.save(entidade);

        auditoriaService.registrar(AcaoAuditoria.EDITAR, "Beneficiario", id,
                "Status alterado de " + statusAnterior + " para " + novoStatus + ". Motivo: " + motivo);
    }

    @Transactional
    public void inativar(Long id, String motivo) {
        Beneficiario entidade = buscarPorId(id);
        entidade.setAtivo(false);
        entidade.setStatus(StatusBeneficiario.INATIVO);
        beneficiarioRepository.save(entidade);

        auditoriaService.registrar(AcaoAuditoria.CANCELAR, "Beneficiario", id,
                "Beneficiário inativado. Motivo: " + motivo);
    }

    public BeneficiarioFichaDto montarFicha(Long id) {
        Beneficiario entidade = buscarPorId(id);

        List<RetiradaCesta> ultimasRetiradas = retiradaCestaRepository
                .findByBeneficiarioIdOrderByMesReferenciaDesc(id)
                .stream()
                .limit(12)
                .toList();

        LocalDate mesAtual = LocalDate.now().withDayOfMonth(1);
        boolean retirouMesAtual = retiradaCestaRepository
                .existsByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(id, mesAtual);

        auditoriaService.registrar(AcaoAuditoria.CONSULTAR, "Beneficiario", id, "Ficha consultada");

        return mapper.paraFicha(entidade, ultimasRetiradas, retirouMesAtual);
    }

    private Specification<Beneficiario> construirSpecification(BeneficiarioFiltro filtro) {
        Specification<Beneficiario> spec = Specification.where(null);

        if (filtro.getNome() != null && !filtro.getNome().isBlank()) {
            String termo = "%" + filtro.getNome().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), termo));
        }
        if (filtro.getCpf() != null && !filtro.getCpf().isBlank()) {
            String cpfNormalizado = CpfUtils.normalizar(filtro.getCpf());
            spec = spec.and((root, query, cb) -> cb.like(root.get("cpf"), "%" + cpfNormalizado + "%"));
        }
        if (filtro.getStatus() != null && !filtro.getStatus().isBlank()) {
            StatusBeneficiario status = parseStatus(filtro.getStatus());
            if (status != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
            }
        }
        if (filtro.getPadrinhoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("padrinho").get("id"), filtro.getPadrinhoId()));
        }
        if (filtro.getAtivo() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ativo"), filtro.getAtivo()));
        }
        if (filtro.getCidade() != null && !filtro.getCidade().isBlank()) {
            String termo = "%" + filtro.getCidade().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Object, Object> endereco = root.join("endereco", JoinType.LEFT);
                return cb.like(cb.lower(endereco.get("cidade")), termo);
            });
        }
        if (filtro.getBairro() != null && !filtro.getBairro().isBlank()) {
            String termo = "%" + filtro.getBairro().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Object, Object> endereco = root.join("endereco", JoinType.LEFT);
                return cb.like(cb.lower(endereco.get("bairro")), termo);
            });
        }
        if (filtro.getPrazoVencendoEmDias() != null) {
            LocalDate hoje = LocalDate.now();
            LocalDate limite = hoje.plusDays(filtro.getPrazoVencendoEmDias());
            spec = spec.and((root, query, cb) -> cb.between(root.get("prazoFinalBeneficio"), hoje, limite));
        }

        return spec;
    }
}
