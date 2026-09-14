package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.RetiradaConfirmacao;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.HistoricoBeneficiarioDto;
import br.com.imgazin.beneficios.dto.RetiradaDetalheDto;
import br.com.imgazin.beneficios.dto.RetiradaListaItemDto;
import br.com.imgazin.beneficios.dto.RetiradaResumoDto;
import br.com.imgazin.beneficios.exception.RetiradaConcorrenteException;
import br.com.imgazin.beneficios.form.RetiradaFiltro;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.mapper.RetiradaMapper;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import br.com.imgazin.beneficios.repository.RetiradaConfirmacaoRepository;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Orquestra a persistência da retirada. Controller nunca grava nada
 * diretamente (ver CLAUDE.md).
 */
@Service
@RequiredArgsConstructor
public class RetiradaService {

    private final RetiradaCestaRepository retiradaCestaRepository;
    private final RetiradaConfirmacaoRepository retiradaConfirmacaoRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final VoluntarioRepository voluntarioRepository;
    private final RetiradaMapper mapper;
    private final BeneficiosAuditoriaService auditoriaService;

    public RetiradaCesta buscarPorId(Long id) {
        return retiradaCestaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Retirada não encontrada: " + id));
    }

    /**
     * Grava a retirada. Concorrência: dois atendentes podem passar pela
     * checagem de "já retirou no mês" ao mesmo tempo — quem perde a corrida
     * do índice único do banco recebe {@link RetiradaConcorrenteException}
     * em vez de um erro 500 (usa {@code saveAndFlush} de propósito, para o
     * INSERT — e a violação de constraint — acontecer DENTRO deste método,
     * onde dá pra capturar).
     */
    @Transactional
    public RetiradaCesta salvar(RetiradaForm form, List<Aviso> avisosConfirmados, String justificativa, String usuarioLogado) {
        Beneficiario beneficiario = beneficiarioRepository.findByCpf(CpfUtils.normalizar(form.getCpfBeneficiario()))
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado para o CPF informado."));

        TipoRetirante tipo = TipoRetirante.valueOf(form.getTipoRetirante());

        Voluntario padrinho = null;
        if (tipo == TipoRetirante.PADRINHO) {
            padrinho = resolverPadrinho(form, beneficiario);
        }

        Voluntario voluntarioEntrega = null;
        if (form.getVoluntarioEntregaId() != null && !form.getVoluntarioEntregaId().isBlank()) {
            voluntarioEntrega = voluntarioRepository.findById(Long.valueOf(form.getVoluntarioEntregaId().trim())).orElse(null);
        }

        RetiradaCesta retirada = mapper.paraEntidade(form, beneficiario, padrinho, voluntarioEntrega, usuarioLogado);

        try {
            retirada = retiradaCestaRepository.saveAndFlush(retirada);
        } catch (DataIntegrityViolationException e) {
            throw new RetiradaConcorrenteException();
        }

        registrarConfirmacoes(retirada, avisosConfirmados, justificativa, usuarioLogado);

        // Regra automática: após gravar, o beneficiário fica RETIRADO_MES_ATUAL.
        beneficiario.setStatus(StatusBeneficiario.RETIRADO_MES_ATUAL);
        beneficiarioRepository.save(beneficiario);

        auditoriaService.registrar(AcaoAuditoria.CRIAR, "RetiradaCesta", retirada.getId(),
                "Retirada registrada para o beneficiário " + beneficiario.getNome() + " (ID " + beneficiario.getId() + "), mês " + retirada.getMesReferencia());

        return retirada;
    }

    /** Se padrinhoId veio vazio no form, usa o padrinho vinculado ao beneficiário (pode ser null). */
    private Voluntario resolverPadrinho(RetiradaForm form, Beneficiario beneficiario) {
        if (form.getPadrinhoId() != null && !form.getPadrinhoId().isBlank()) {
            return voluntarioRepository.findById(Long.valueOf(form.getPadrinhoId().trim())).orElse(null);
        }
        return beneficiario.getPadrinho();
    }

    private void registrarConfirmacoes(RetiradaCesta retirada, List<Aviso> avisosConfirmados, String justificativa, String usuarioLogado) {
        if (avisosConfirmados == null || avisosConfirmados.isEmpty()) {
            return;
        }
        LocalDateTime agora = LocalDateTime.now();
        for (Aviso aviso : avisosConfirmados) {
            RetiradaConfirmacao confirmacao = new RetiradaConfirmacao();
            confirmacao.setRetirada(retirada);
            confirmacao.setCodigoAviso(aviso.getCodigo());
            confirmacao.setSeveridade(aviso.getSeveridade());
            confirmacao.setMensagem(aviso.getMensagem());
            confirmacao.setReferenciaId(aviso.getReferenciaId());
            confirmacao.setConfirmadoPor(usuarioLogado);
            confirmacao.setConfirmadoEm(agora);
            confirmacao.setJustificativa(justificativa);
            retiradaConfirmacaoRepository.save(confirmacao);
        }
    }

    @Transactional
    public void cancelar(Long id, String motivo, String usuarioLogado) {
        RetiradaCesta retirada = buscarPorId(id);
        retirada.setCancelada(true);
        retirada.setMotivoCancelamento(motivo);
        retirada.setCanceladaEm(LocalDateTime.now());
        retirada.setCanceladaPor(usuarioLogado);
        retiradaCestaRepository.save(retirada);

        // Se essa era a única retirada válida do mês e o beneficiário ainda está
        // marcado como "já retirou", devolve para apto — sem esperar o job do dia 1º.
        Beneficiario beneficiario = retirada.getBeneficiario();
        boolean aindaTemRetiradaValidaNoMes = retiradaCestaRepository
                .existsByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(beneficiario.getId(), retirada.getMesReferencia());
        if (!aindaTemRetiradaValidaNoMes && beneficiario.getStatus() == StatusBeneficiario.RETIRADO_MES_ATUAL) {
            beneficiario.setStatus(StatusBeneficiario.APTO_MES_ATUAL);
            beneficiarioRepository.save(beneficiario);
        }

        auditoriaService.registrar(AcaoAuditoria.CANCELAR, "RetiradaCesta", id, "Retirada cancelada. Motivo: " + motivo);
    }

    public Page<RetiradaListaItemDto> listar(RetiradaFiltro filtro, Pageable pageable) {
        return retiradaCestaRepository.findAll(construirSpecification(filtro), pageable).map(mapper::paraItemLista);
    }

    public RetiradaDetalheDto detalhar(Long id) {
        RetiradaCesta retirada = buscarPorId(id);
        List<RetiradaConfirmacao> confirmacoes = retiradaConfirmacaoRepository.findByRetiradaId(id);
        auditoriaService.registrar(AcaoAuditoria.CONSULTAR, "RetiradaCesta", id, "Detalhe consultado");
        return mapper.paraDetalhe(retirada, confirmacoes);
    }

    public RetiradaDetalheDto comprovante(Long id) {
        RetiradaCesta retirada = buscarPorId(id);
        List<RetiradaConfirmacao> confirmacoes = retiradaConfirmacaoRepository.findByRetiradaId(id);
        return mapper.paraDetalhe(retirada, confirmacoes);
    }

    /**
     * Resumo do histórico de um beneficiário (entregável da Etapa 3): total de
     * cestas, meses consecutivos com retirada, meses sem retirar e distribuição
     * por tipo de retirante — só considerando retiradas válidas (não canceladas).
     */
    public HistoricoBeneficiarioDto historicoDoBeneficiario(Long beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado: " + beneficiarioId));

        List<RetiradaCesta> validas = retiradaCestaRepository
                .findByBeneficiarioIdAndCanceladaFalseOrderByMesReferenciaDesc(beneficiarioId);

        HistoricoBeneficiarioDto dto = new HistoricoBeneficiarioDto();
        dto.setBeneficiarioId(beneficiario.getId());
        dto.setBeneficiarioNome(beneficiario.getNome());
        dto.setBeneficiarioCpf(beneficiario.getCpf());

        dto.setTotalCestas(validas.stream().mapToLong(RetiradaCesta::getQuantidadeCestas).sum());
        dto.setMesesConsecutivos(calcularMesesConsecutivos(validas));
        dto.setMesesSemRetirar(calcularMesesSemRetirar(validas, beneficiario));
        dto.setDistribuicaoPorTipoRetirante(calcularDistribuicaoPorTipo(validas));

        dto.setRetiradas(retiradaCestaRepository.findByBeneficiarioIdOrderByMesReferenciaDesc(beneficiarioId).stream()
                .map(r -> new RetiradaResumoDto(r.getId(), r.getMesReferencia(), r.getDataRetirada(), r.getTipoRetirante(), r.getRetiranteNome(), r.isCancelada()))
                .toList());

        return dto;
    }

    private int calcularMesesConsecutivos(List<RetiradaCesta> validasOrdenadasDesc) {
        if (validasOrdenadasDesc.isEmpty()) {
            return 0;
        }
        int contador = 1;
        YearMonth anterior = YearMonth.from(validasOrdenadasDesc.get(0).getMesReferencia());
        for (int i = 1; i < validasOrdenadasDesc.size(); i++) {
            YearMonth atual = YearMonth.from(validasOrdenadasDesc.get(i).getMesReferencia());
            if (atual.equals(anterior.minusMonths(1))) {
                contador++;
                anterior = atual;
            } else if (atual.equals(anterior)) {
                // duas retiradas válidas no mesmo mês (JA_RETIROU_NO_MES confirmado) não conta duas vezes
            } else {
                break;
            }
        }
        return contador;
    }

    /** Meses corridos, sem retirada válida, entre a última retirada (ou o cadastro) e hoje. */
    private int calcularMesesSemRetirar(List<RetiradaCesta> validasOrdenadasDesc, Beneficiario beneficiario) {
        YearMonth referencia;
        if (!validasOrdenadasDesc.isEmpty()) {
            referencia = YearMonth.from(validasOrdenadasDesc.get(0).getMesReferencia());
        } else if (beneficiario.getCriadoEm() != null) {
            referencia = YearMonth.from(beneficiario.getCriadoEm());
        } else {
            return 0;
        }
        long meses = ChronoUnit.MONTHS.between(referencia, YearMonth.now());
        return (int) Math.max(0, meses);
    }

    private Map<TipoRetirante, Long> calcularDistribuicaoPorTipo(List<RetiradaCesta> validas) {
        Map<TipoRetirante, Long> distribuicao = new EnumMap<>(TipoRetirante.class);
        for (RetiradaCesta retirada : validas) {
            distribuicao.merge(retirada.getTipoRetirante(), 1L, Long::sum);
        }
        return distribuicao;
    }

    private Specification<RetiradaCesta> construirSpecification(RetiradaFiltro filtro) {
        Specification<RetiradaCesta> spec = Specification.where(null);

        if (!filtro.isIncluirCanceladas()) {
            spec = spec.and((root, query, cb) -> cb.isFalse(root.get("cancelada")));
        }
        if (filtro.getMes() != null && !filtro.getMes().isBlank()) {
            try {
                LocalDate mes = YearMonth.parse(filtro.getMes()).atDay(1);
                spec = spec.and((root, query, cb) -> cb.equal(root.get("mesReferencia"), mes));
            } catch (Exception ignored) {
                // filtro mal formado é apenas ignorado, não quebra a listagem
            }
        }
        if (filtro.getDataInicio() != null && !filtro.getDataInicio().isBlank()) {
            try {
                LocalDateTime inicio = LocalDate.parse(filtro.getDataInicio()).atStartOfDay();
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataRetirada"), inicio));
            } catch (Exception ignored) {
            }
        }
        if (filtro.getDataFim() != null && !filtro.getDataFim().isBlank()) {
            try {
                LocalDateTime fim = LocalDate.parse(filtro.getDataFim()).atTime(LocalTime.MAX);
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataRetirada"), fim));
            } catch (Exception ignored) {
            }
        }
        if (filtro.getCpf() != null && !filtro.getCpf().isBlank()) {
            String cpfNormalizado = CpfUtils.normalizar(filtro.getCpf());
            spec = spec.and((root, query, cb) -> cb.like(root.get("beneficiario").get("cpf"), "%" + cpfNormalizado + "%"));
        }
        if (filtro.getPadrinhoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("padrinho").get("id"), filtro.getPadrinhoId()));
        }
        if (filtro.getVoluntarioId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("voluntarioEntrega").get("id"), filtro.getVoluntarioId()));
        }
        if (filtro.getTipoRetirante() != null && !filtro.getTipoRetirante().isBlank()) {
            try {
                TipoRetirante tipo = TipoRetirante.valueOf(filtro.getTipoRetirante());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoRetirante"), tipo));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (filtro.getCidade() != null && !filtro.getCidade().isBlank()) {
            String termo = "%" + filtro.getCidade().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Object, Object> endereco = root.join("beneficiario", JoinType.LEFT).join("endereco", JoinType.LEFT);
                return cb.like(cb.lower(endereco.get("cidade")), termo);
            });
        }
        if (filtro.getBairro() != null && !filtro.getBairro().isBlank()) {
            String termo = "%" + filtro.getBairro().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Object, Object> endereco = root.join("beneficiario", JoinType.LEFT).join("endereco", JoinType.LEFT);
                return cb.like(cb.lower(endereco.get("bairro")), termo);
            });
        }

        return spec;
    }
}
