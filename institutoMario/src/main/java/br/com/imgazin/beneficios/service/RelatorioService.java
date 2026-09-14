package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.ConfirmacaoRelatorioDto;
import br.com.imgazin.beneficios.dto.DashboardDto;
import br.com.imgazin.beneficios.dto.InativoRecorrenteDto;
import br.com.imgazin.beneficios.dto.LinhaTempoBeneficiarioDto;
import br.com.imgazin.beneficios.dto.PendenteDto;
import br.com.imgazin.beneficios.dto.PerfilSocialDto;
import br.com.imgazin.beneficios.dto.PorPadrinhoDto;
import br.com.imgazin.beneficios.dto.PorRegiaoDto;
import br.com.imgazin.beneficios.dto.PorVoluntarioDto;
import br.com.imgazin.beneficios.dto.PrazoVencendoDto;
import br.com.imgazin.beneficios.dto.RetiradaMensalDto;
import br.com.imgazin.beneficios.dto.RetiradaPorTerceiroDto;
import br.com.imgazin.beneficios.dto.VoluntarioAnaliticoDto;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RelatorioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Toda consulta agrega no banco (via {@link RelatorioRepository}), nunca
 * carregando lista de entidades só para somar em Java — exceção feita a
 * pequenas listas já pequenas por natureza (pendentes de um mês, histórico
 * de um beneficiário), onde o N+1 é aceitável para o volume de uma ONG.
 */
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final RetiradaCestaRepository retiradaCestaRepository;
    private final VoluntarioRepository voluntarioRepository;
    private final BeneficiosAuditoriaService auditoriaService;

    public DashboardDto dashboard(YearMonth mes) {
        LocalDate mesData = mes.atDay(1);

        long pendentes = relatorioRepository.contarPorStatusAtivo(StatusBeneficiario.APTO_MES_ATUAL);
        long retiradasDoMes = relatorioRepository.contarBeneficiariosDistintosComRetiradaNoMes(mesData);
        // Definição estrutural: aptos = pendentes + já retiraram — garante o
        // critério de aceite "pendentes + retiradas do mês = total de aptos do mês".
        long aptosNoMes = pendentes + retiradasDoMes;

        DashboardDto dto = new DashboardDto();
        dto.setMes(mes.toString());
        dto.setBeneficiariosAtivos(relatorioRepository.contarBeneficiariosAtivos());
        dto.setAptosNoMes(aptosNoMes);
        dto.setRetiradasDoMes(retiradasDoMes);
        dto.setPercentualAdesao(percentual(retiradasDoMes, aptosNoMes));
        dto.setPendentes(pendentes);
        dto.setCestasEntregues(relatorioRepository.somarCestasNoMes(mesData));
        dto.setNovosCadastros(relatorioRepository.contarNovosCadastros(mesData.atStartOfDay(), mes.atEndOfMonth().atTime(23, 59, 59)));
        dto.setBeneficiosVencendo30Dias(relatorioRepository.contarPrazoVencendoEm(30));
        dto.setVoluntariosAtivos(relatorioRepository.contarVoluntariosAtivos());
        dto.setPadrinhosAtivos(relatorioRepository.contarPadrinhosAtivos());

        dto.setSerieRetiradasPorMes(retiradasMensais(mes.minusMonths(5), mes));
        dto.setRetiradasPorBairro(relatorioRepository.porRegiao("bairro", mesData, mesData));

        Map<String, Long> distribuicao = new LinkedHashMap<>();
        for (Object[] linha : relatorioRepository.distribuicaoTipoRetiranteNoMes(mesData)) {
            distribuicao.put(((TipoRetirante) linha[0]).getDescricao(), (Long) linha[1]);
        }
        dto.setDistribuicaoTipoRetirante(distribuicao);

        return dto;
    }

    public List<RetiradaMensalDto> retiradasMensais(YearMonth inicio, YearMonth fim) {
        List<Object[]> linhas = relatorioRepository.serieRetiradasPorMes(inicio.atDay(1), fim.atDay(1));
        long totalAtivos = relatorioRepository.contarBeneficiariosAtivos();

        List<RetiradaMensalDto> resultado = new ArrayList<>();
        for (Object[] linha : linhas) {
            LocalDate mesData = (LocalDate) linha[0];
            long retiradas = (Long) linha[1];
            long cestas = ((Number) linha[2]).longValue();
            long distintos = (Long) linha[3];
            // Sem tabela de resumo histórico (ver CLAUDE.md: melhoria futura), a
            // "adesão" de meses passados é aproximada pelo total de ativos atual.
            resultado.add(new RetiradaMensalDto(YearMonth.from(mesData), retiradas, cestas, distintos, percentual(distintos, totalAtivos)));
        }
        return resultado;
    }

    public List<PendenteDto> pendentes(YearMonth mes) {
        List<Beneficiario> aptos = beneficiarioRepository.findByStatus(StatusBeneficiario.APTO_MES_ATUAL).stream()
                .filter(Beneficiario::isAtivo)
                .toList();

        List<PendenteDto> resultado = new ArrayList<>();
        for (Beneficiario b : aptos) {
            RetiradaCesta ultima = retiradaCestaRepository.findByBeneficiarioIdAndCanceladaFalseOrderByMesReferenciaDesc(b.getId())
                    .stream().findFirst().orElse(null);
            LocalDate ultimaData = ultima == null ? null : ultima.getMesReferencia();
            long diasSemRetirar = ultimaData == null
                    ? ChronoUnit.DAYS.between(b.getCriadoEm() != null ? b.getCriadoEm().toLocalDate() : LocalDate.now(), LocalDate.now())
                    : ChronoUnit.DAYS.between(ultimaData, LocalDate.now());

            resultado.add(new PendenteDto(
                    b.getId(), b.getNome(), b.getCpf(), b.getTelefone(),
                    b.getEndereco() == null ? null : b.getEndereco().getBairro(),
                    b.getPadrinho() == null ? null : b.getPadrinho().getNome(),
                    ultimaData, diasSemRetirar));
        }
        return resultado;
    }

    public List<PorPadrinhoDto> porPadrinho(YearMonth inicio, YearMonth fim) {
        LocalDate dataInicio = inicio.atDay(1);
        LocalDate dataFim = fim.atEndOfMonth();

        List<Voluntario> padrinhos = voluntarioRepository.findByPadrinhoTrueAndAtivoTrue();
        List<PorPadrinhoDto> resultado = new ArrayList<>();
        for (Voluntario padrinho : padrinhos) {
            long apadrinhados = relatorioRepository.contarApadrinhados(padrinho.getId());
            long retiradasPeriodo = relatorioRepository.contarRetiradasDeApadrinhadosNoPeriodo(padrinho.getId(), dataInicio, dataFim);
            long retiradasPeloProprio = relatorioRepository.contarRetiradasFeitasPeloPadrinho(padrinho.getId(), dataInicio, dataFim);
            resultado.add(new PorPadrinhoDto(padrinho.getId(), padrinho.getNome(), apadrinhados, retiradasPeriodo,
                    retiradasPeloProprio, percentual(retiradasPeriodo, apadrinhados)));
        }
        return resultado;
    }

    public List<PorVoluntarioDto> porVoluntario(YearMonth inicio, YearMonth fim) {
        LocalDate dataInicio = inicio.atDay(1);
        LocalDate dataFim = fim.atEndOfMonth();

        return voluntarioRepository.findByAtivoTrue().stream()
                .map(v -> new PorVoluntarioDto(v.getId(), v.getNome(), v.getAreaAtuacao(), v.getSetor(),
                        relatorioRepository.contarEntregasNoPeriodo(v.getId(), dataInicio, dataFim)))
                .toList();
    }

    public List<PorRegiaoDto> porRegiao(String agruparPor, YearMonth inicio, YearMonth fim) {
        String coluna = ("cidade".equals(agruparPor) || "uf".equals(agruparPor)) ? agruparPor : "bairro";
        return relatorioRepository.porRegiao(coluna, inicio.atDay(1), fim.atEndOfMonth());
    }

    public PerfilSocialDto perfilSocial() {
        PerfilSocialDto dto = new PerfilSocialDto();
        long total = relatorioRepository.contarBeneficiariosAtivos();
        dto.setTotalBeneficiarios(total);
        dto.setMediaPessoasResidencia(relatorioRepository.mediaPessoasResidencia());
        dto.setPercentualComFilhos(percentual(relatorioRepository.contarComFilhos(), total));
        dto.setMediaFilhos(relatorioRepository.mediaQuantidadeFilhos());
        dto.setPercentualFilhosComDeficiencia(percentual(relatorioRepository.contarFilhosComDeficiencia(), total));
        dto.setPercentualFamiliarComDeficiencia(percentual(relatorioRepository.contarFamiliarComDeficiencia(), total));
        dto.setPercentualSemTrabalho(percentual(relatorioRepository.contarSemTrabalho(), total));

        Map<String, Long> faixas = new LinkedHashMap<>();
        faixas.put("0-11", 0L);
        faixas.put("12-17", 0L);
        faixas.put("18-29", 0L);
        faixas.put("30-59", 0L);
        faixas.put("60+", 0L);
        for (int idade : relatorioRepository.idadesBeneficiariosAtivos()) {
            String faixa = idade <= 11 ? "0-11" : idade <= 17 ? "12-17" : idade <= 29 ? "18-29" : idade <= 59 ? "30-59" : "60+";
            faixas.merge(faixa, 1L, Long::sum);
        }
        dto.setFaixasEtarias(faixas);
        return dto;
    }

    public List<LinhaTempoBeneficiarioDto> historicoBeneficiario(Long beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado: " + beneficiarioId));

        Map<YearMonth, RetiradaCesta> porMes = new LinkedHashMap<>();
        for (RetiradaCesta r : retiradaCestaRepository.findByBeneficiarioIdOrderByMesReferenciaDesc(beneficiarioId)) {
            porMes.putIfAbsent(YearMonth.from(r.getMesReferencia()), r); // se houver 2 no mês, prioriza a mais recente inserida primeiro na lista já ordenada
        }

        // Janela: desde o cadastro, limitada a no máximo 24 meses para trás.
        YearMonth atual = YearMonth.now();
        YearMonth criadoMes = beneficiario.getCriadoEm() != null ? YearMonth.from(beneficiario.getCriadoEm()) : atual.minusMonths(11);
        YearMonth limiteMaximo = atual.minusMonths(23);
        YearMonth inicioJanela = criadoMes.isAfter(limiteMaximo) ? criadoMes : limiteMaximo;

        List<LinhaTempoBeneficiarioDto> linha = new ArrayList<>();
        for (YearMonth referencia = atual; !referencia.isBefore(inicioJanela); referencia = referencia.minusMonths(1)) {
            RetiradaCesta r = porMes.get(referencia);
            if (r == null) {
                linha.add(new LinhaTempoBeneficiarioDto(referencia, "NAO_RETIROU", null));
            } else if (r.isCancelada()) {
                linha.add(new LinhaTempoBeneficiarioDto(referencia, "CANCELADA", r.getTipoRetirante()));
            } else {
                linha.add(new LinhaTempoBeneficiarioDto(referencia, "RETIROU", r.getTipoRetirante()));
            }
        }

        auditoriaService.registrar(AcaoAuditoria.CONSULTAR, "Beneficiario", beneficiarioId, "Relatório de histórico consultado");
        return linha;
    }

    public List<PrazoVencendoDto> prazosVencendo(int dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        return beneficiarioRepository.findByAtivoTrue().stream()
                .filter(b -> b.getPrazoFinalBeneficio() != null && !b.getPrazoFinalBeneficio().isAfter(limite))
                .map(b -> new PrazoVencendoDto(b.getId(), b.getNome(), b.getCpf(), b.getPrazoFinalBeneficio(),
                        ChronoUnit.DAYS.between(LocalDate.now(), b.getPrazoFinalBeneficio())))
                .sorted((a, c) -> a.getPrazoFinalBeneficio().compareTo(c.getPrazoFinalBeneficio()))
                .toList();
    }

    public List<InativoRecorrenteDto> inativosRecorrentes(int meses) {
        YearMonth limite = YearMonth.now().minusMonths(meses);
        List<InativoRecorrenteDto> resultado = new ArrayList<>();

        for (Beneficiario b : beneficiarioRepository.findByAtivoTrue()) {
            RetiradaCesta ultima = retiradaCestaRepository.findByBeneficiarioIdAndCanceladaFalseOrderByMesReferenciaDesc(b.getId())
                    .stream().findFirst().orElse(null);

            YearMonth mesUltima = ultima == null ? null : YearMonth.from(ultima.getMesReferencia());
            boolean semRetiradaRecente = mesUltima == null || mesUltima.isBefore(limite);

            if (semRetiradaRecente) {
                long mesesSemRetirar = mesUltima == null
                        ? ChronoUnit.MONTHS.between(YearMonth.from(b.getCriadoEm() != null ? b.getCriadoEm() : LocalDateTime.now()), YearMonth.now())
                        : ChronoUnit.MONTHS.between(mesUltima, YearMonth.now());
                resultado.add(new InativoRecorrenteDto(b.getId(), b.getNome(), b.getCpf(),
                        ultima == null ? null : ultima.getMesReferencia(), mesesSemRetirar));
            }
        }
        return resultado;
    }

    public List<RetiradaPorTerceiroDto> retiradasPorTerceiros(YearMonth inicio, YearMonth fim) {
        List<RetiradaPorTerceiroDto> resultado = new ArrayList<>();
        for (Object[] linha : relatorioRepository.distribuicaoTipoRetiranteTerceirosNoPeriodo(inicio.atDay(1), fim.atEndOfMonth())) {
            resultado.add(new RetiradaPorTerceiroDto((TipoRetirante) linha[0], (Long) linha[1]));
        }
        return resultado;
    }

    public List<ConfirmacaoRelatorioDto> confirmacoes(LocalDateTime inicio, LocalDateTime fim, String codigo) {
        List<ConfirmacaoRelatorioDto> resultado = new ArrayList<>();
        for (Object[] linha : relatorioRepository.confirmacoesNoPeriodo(inicio, fim, codigo)) {
            resultado.add(new ConfirmacaoRelatorioDto(
                    (LocalDateTime) linha[0], (String) linha[1],
                    (br.com.imgazin.beneficios.domain.SeveridadeAviso) linha[2],
                    (Long) linha[3], (String) linha[4], (String) linha[5], (String) linha[6]));
        }
        return resultado;
    }

    public List<VoluntarioAnaliticoDto> voluntariosAnalitico(Boolean ativo, String areaAtuacao) {
        return voluntarioRepository.findAll().stream()
                .filter(v -> ativo == null || v.isAtivo() == ativo)
                .filter(v -> areaAtuacao == null || areaAtuacao.isBlank()
                        || (v.getAreaAtuacao() != null && v.getAreaAtuacao().toLowerCase().contains(areaAtuacao.toLowerCase())))
                .map(v -> new VoluntarioAnaliticoDto(v.getId(), v.getNome(), v.getAreaAtuacao(), v.getSetor(),
                        v.isPadrinho(), v.isAtivo(), relatorioRepository.contarEntregasTotal(v.getId())))
                .toList();
    }

    private double percentual(long parte, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round((parte * 10000.0) / total) / 100.0;
    }
}
