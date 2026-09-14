package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.ApadrinhadoResumoDto;
import br.com.imgazin.beneficios.dto.EntregaMesDto;
import br.com.imgazin.beneficios.dto.VoluntarioAtuacaoDto;
import br.com.imgazin.beneficios.dto.VoluntarioListaItemDto;
import br.com.imgazin.beneficios.exception.CpfDuplicadoException;
import br.com.imgazin.beneficios.form.VoluntarioFiltro;
import br.com.imgazin.beneficios.form.VoluntarioForm;
import br.com.imgazin.beneficios.mapper.VoluntarioMapper;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoluntarioService {

    private final VoluntarioRepository voluntarioRepository;
    private final VoluntarioMapper mapper;
    private final BeneficiosAuditoriaService auditoriaService;
    private final RetiradaCestaRepository retiradaCestaRepository;
    private final ApadrinhamentoService apadrinhamentoService;

    public Voluntario buscarPorId(Long id) {
        return voluntarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voluntário não encontrado: " + id));
    }

    public Page<VoluntarioListaItemDto> listar(VoluntarioFiltro filtro, Pageable pageable) {
        return voluntarioRepository.findAll(construirSpecification(filtro), pageable).map(mapper::paraItemLista);
    }

    /** CPF é opcional — só verifica duplicidade se foi informado. */
    public void verificarCpfDuplicado(VoluntarioForm form) {
        if (form.getCpf() == null || form.getCpf().isBlank()) {
            return;
        }
        String normalizado = CpfUtils.normalizar(form.getCpf());
        voluntarioRepository.findByCpf(normalizado).ifPresent(existente -> {
            if (form.getId() == null || !existente.getId().equals(form.getId())) {
                throw new CpfDuplicadoException(existente.getId());
            }
        });
    }

    @Transactional
    public Voluntario salvar(VoluntarioForm form, List<Aviso> avisosConfirmados, String justificativa) {
        boolean novoCadastro = form.getId() == null;
        Voluntario entidade = novoCadastro ? new Voluntario() : buscarPorId(form.getId());

        mapper.aplicarNaEntidade(form, entidade);
        entidade = voluntarioRepository.save(entidade);

        AcaoAuditoria acao = novoCadastro ? AcaoAuditoria.CRIAR : AcaoAuditoria.EDITAR;
        StringBuilder detalhes = new StringBuilder("Voluntário ")
                .append(novoCadastro ? "cadastrado" : "atualizado")
                .append(" (ID ").append(entidade.getId()).append(")");
        if (avisosConfirmados != null && !avisosConfirmados.isEmpty()) {
            detalhes.append(" | Avisos confirmados: ")
                    .append(avisosConfirmados.stream().map(Aviso::getCodigo).collect(Collectors.joining(", ")));
            if (justificativa != null && !justificativa.isBlank()) {
                detalhes.append(" | Justificativa: ").append(justificativa);
            }
        }
        auditoriaService.registrar(acao, "Voluntario", entidade.getId(), detalhes.toString());

        return entidade;
    }

    /** Tela `voluntarios/atuacao`: entregas por mês, apadrinhados e última atividade. */
    public VoluntarioAtuacaoDto montarAtuacao(Long id) {
        Voluntario voluntario = buscarPorId(id);

        List<RetiradaCesta> entregas = retiradaCestaRepository
                .findByVoluntarioEntregaIdAndCanceladaFalseOrderByDataRetiradaDesc(id);

        Map<YearMonth, Long> porMes = new TreeMap<>(Comparator.reverseOrder());
        for (RetiradaCesta entrega : entregas) {
            porMes.merge(YearMonth.from(entrega.getMesReferencia()), 1L, Long::sum);
        }
        List<EntregaMesDto> entregasPorMes = porMes.entrySet().stream()
                .map(entrada -> new EntregaMesDto(entrada.getKey(), entrada.getValue()))
                .toList();

        List<ApadrinhadoResumoDto> apadrinhados = apadrinhamentoService.listarApadrinhados(id);

        var ultimaAtividade = entregas.isEmpty() ? null : entregas.get(0).getDataRetirada();

        auditoriaService.registrar(AcaoAuditoria.CONSULTAR, "Voluntario", id, "Tela de atuação consultada");

        return mapper.paraAtuacao(voluntario, entregasPorMes, apadrinhados, ultimaAtividade);
    }

    @Transactional
    public void inativar(Long id, String motivo) {
        Voluntario voluntario = buscarPorId(id);
        voluntario.setAtivo(false);
        voluntarioRepository.save(voluntario);
        auditoriaService.registrar(AcaoAuditoria.CANCELAR, "Voluntario", id, "Voluntário inativado. Motivo: " + motivo);
    }

    private Specification<Voluntario> construirSpecification(VoluntarioFiltro filtro) {
        Specification<Voluntario> spec = Specification.where(null);

        if (filtro.getNome() != null && !filtro.getNome().isBlank()) {
            String termo = "%" + filtro.getNome().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), termo));
        }
        if (filtro.getCpf() != null && !filtro.getCpf().isBlank()) {
            String cpfNormalizado = CpfUtils.normalizar(filtro.getCpf());
            spec = spec.and((root, query, cb) -> cb.like(root.get("cpf"), "%" + cpfNormalizado + "%"));
        }
        if (filtro.getAreaAtuacao() != null && !filtro.getAreaAtuacao().isBlank()) {
            String termo = "%" + filtro.getAreaAtuacao().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("areaAtuacao")), termo));
        }
        if (filtro.getSetor() != null && !filtro.getSetor().isBlank()) {
            String termo = "%" + filtro.getSetor().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("setor")), termo));
        }
        if (filtro.getIsPadrinho() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("padrinho"), filtro.getIsPadrinho()));
        }
        if (filtro.getAtivo() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ativo"), filtro.getAtivo()));
        }

        return spec;
    }
}
