package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.dto.*;
import br.com.imgazin.beneficios.form.RelatorioFiltro;
import br.com.imgazin.beneficios.service.BeneficiosAuditoriaService;
import br.com.imgazin.beneficios.service.RelatorioService;
import br.com.imgazin.beneficios.service.export.ExportadorCsvService;
import br.com.imgazin.beneficios.service.export.ExportadorRelatorio;
import br.com.imgazin.beneficios.service.export.ExportadorXlsxService;
import br.com.imgazin.beneficios.util.CpfUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Um relatório por rota, todos sob {@code /beneficios/relatorios}. Controller
 * fino: monta o filtro, chama {@link RelatorioService}, ou popula a view ou
 * — se {@code formato} vier preenchido — grava direto na resposta via
 * {@link ExportadorRelatorio} e devolve {@code null} (sem view a renderizar).
 */
// Nome de bean explícito: já existe um RelatorioController (não relacionado, de
// notas/provas) em edu.unialfa.institutoMario.controller, e o @ComponentScan da
// aplicação varre os dois pacotes — sem isso o Spring recusa subir o contexto por
// nomes de bean duplicados.
@Controller("beneficiosRelatorioController")
@RequestMapping("/beneficios/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private static final DateTimeFormatter FORMATO_ARQUIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int TAMANHO_PAGINA_PADRAO = 50;
    private static final int TAMANHO_PAGINA_MAXIMO = 500;

    private final RelatorioService relatorioService;
    private final ExportadorCsvService exportadorCsv;
    private final ExportadorXlsxService exportadorXlsx;
    private final BeneficiosAuditoriaService auditoriaService;

    @GetMapping
    public String indice(Model model) {
        model.addAttribute("tituloPagina", "Relatórios");
        return "beneficios/relatorios/indice";
    }

    @GetMapping("/dashboard")
    public String dashboard(RelatorioFiltro filtro, Model model) {
        YearMonth mes = parseMes(filtro.getMes());
        DashboardDto dto = relatorioService.dashboard(mes);
        model.addAttribute("tituloPagina", "Dashboard");
        model.addAttribute("mes", mes.toString());
        model.addAttribute("dashboard", dto);

        // Listas simples prontas para o Chart.js — monta aqui, não na view, para
        // não depender de projeção SpringEL complicada dentro do th:inline.
        model.addAttribute("labelsMeses", dto.getSerieRetiradasPorMes().stream().map(d -> d.getMes().toString()).toList());
        model.addAttribute("valoresMeses", dto.getSerieRetiradasPorMes().stream().map(RetiradaMensalDto::getRetiradas).toList());
        model.addAttribute("labelsBairros", dto.getRetiradasPorBairro().stream().map(PorRegiaoDto::getRegiao).toList());
        model.addAttribute("valoresBairros", dto.getRetiradasPorBairro().stream().map(PorRegiaoDto::getRetiradas).toList());
        model.addAttribute("labelsTipos", new ArrayList<>(dto.getDistribuicaoTipoRetirante().keySet()));
        model.addAttribute("valoresTipos", new ArrayList<>(dto.getDistribuicaoTipoRetirante().values()));

        return "beneficios/relatorios/dashboard";
    }

    @GetMapping("/retiradas-mensais")
    public String retiradasMensais(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        YearMonth inicio = parseMes(filtro.getMesInicio() != null ? filtro.getMesInicio() : filtro.getMes(), YearMonth.now().minusMonths(11));
        YearMonth fim = parseMes(filtro.getMesFim(), YearMonth.now());
        List<RetiradaMensalDto> dados = relatorioService.retiradasMensais(inicio, fim);

        if (exportarSeSolicitado(filtro, "retiradas_mensais", inicio + "_a_" + fim, "Retiradas mensais",
                List.of("Mês", "Retiradas", "Cestas", "Beneficiários distintos", "% adesão"),
                dados.stream().map(d -> List.of(d.getMes().toString(), String.valueOf(d.getRetiradas()),
                        String.valueOf(d.getCestas()), String.valueOf(d.getBeneficiariosDistintos()), d.getPercentualAdesao() + "%")).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Retiradas mensais");
        model.addAttribute("filtro", filtro);
        model.addAttribute("dados", dados);
        return "beneficios/relatorios/retiradas-mensais";
    }

    @GetMapping("/pendentes")
    public String pendentes(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        YearMonth mes = parseMes(filtro.getMes());
        List<PendenteDto> dados = relatorioService.pendentes(mes);

        if (exportarSeSolicitado(filtro, "pendentes", mes.toString(), "Pendentes",
                List.of("Nome", "CPF", "Telefone", "Bairro", "Padrinho", "Última retirada", "Dias sem retirar"),
                dados.stream().map(d -> List.of(d.getNome(), cpfParaExportacao(d.getCpf()), nvl(d.getTelefone()),
                        nvl(d.getBairro()), nvl(d.getPadrinhoNome()), d.getUltimaRetirada() == null ? "nunca" : d.getUltimaRetirada().toString(),
                        String.valueOf(d.getDiasSemRetirar()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Pendentes do mês");
        model.addAttribute("mes", mes.toString());
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/pendentes";
    }

    @GetMapping("/por-padrinho")
    public String porPadrinho(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        YearMonth inicio = parseMes(filtro.getMesInicio(), YearMonth.now());
        YearMonth fim = parseMes(filtro.getMesFim(), YearMonth.now());
        List<PorPadrinhoDto> dados = relatorioService.porPadrinho(inicio, fim);

        if (exportarSeSolicitado(filtro, "por_padrinho", inicio + "_a_" + fim, "Por padrinho",
                List.of("Padrinho", "Apadrinhados", "Retiradas no período", "Entregues pelo próprio", "% adesão"),
                dados.stream().map(d -> List.of(d.getNome(), String.valueOf(d.getApadrinhados()),
                        String.valueOf(d.getRetiradasPeriodo()), String.valueOf(d.getRetiradasPeloProprioPadrinho()), d.getTaxaAdesao() + "%")).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Retiradas por padrinho");
        model.addAttribute("filtro", filtro);
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/por-padrinho";
    }

    @GetMapping("/por-voluntario")
    public String porVoluntario(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        YearMonth inicio = parseMes(filtro.getMesInicio(), YearMonth.now());
        YearMonth fim = parseMes(filtro.getMesFim(), YearMonth.now());
        List<PorVoluntarioDto> dados = relatorioService.porVoluntario(inicio, fim);

        if (exportarSeSolicitado(filtro, "por_voluntario", inicio + "_a_" + fim, "Por voluntário",
                List.of("Voluntário", "Área de atuação", "Setor", "Entregas"),
                dados.stream().map(d -> List.of(d.getNome(), nvl(d.getAreaAtuacao()), nvl(d.getSetor()), String.valueOf(d.getEntregas()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Entregas por voluntário");
        model.addAttribute("filtro", filtro);
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/por-voluntario";
    }

    @GetMapping("/por-regiao")
    public String porRegiao(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        String agruparPor = filtro.getAgruparPor() == null ? "bairro" : filtro.getAgruparPor();
        YearMonth inicio = parseMes(filtro.getMesInicio(), YearMonth.now());
        YearMonth fim = parseMes(filtro.getMesFim(), YearMonth.now());
        List<PorRegiaoDto> dados = relatorioService.porRegiao(agruparPor, inicio, fim);

        if (exportarSeSolicitado(filtro, "por_regiao", inicio + "_a_" + fim, "Por região",
                List.of(agruparPor.substring(0, 1).toUpperCase() + agruparPor.substring(1), "Beneficiários", "Retiradas"),
                dados.stream().map(d -> List.of(d.getRegiao(), String.valueOf(d.getBeneficiarios()), String.valueOf(d.getRetiradas()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Distribuição geográfica");
        model.addAttribute("filtro", filtro);
        model.addAttribute("agruparPor", agruparPor);
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/por-regiao";
    }

    @GetMapping("/perfil-social")
    public String perfilSocial(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        PerfilSocialDto dto = relatorioService.perfilSocial();

        if (exportarSeSolicitado(filtro, "perfil_social", LocalDate.now().toString(), "Perfil social",
                List.of("Indicador", "Valor"),
                List.of(
                        List.of("Total de beneficiários ativos", String.valueOf(dto.getTotalBeneficiarios())),
                        List.of("Média de pessoas por residência", String.valueOf(dto.getMediaPessoasResidencia())),
                        List.of("% com filhos", dto.getPercentualComFilhos() + "%"),
                        List.of("Média de filhos", String.valueOf(dto.getMediaFilhos())),
                        List.of("% filhos com deficiência", dto.getPercentualFilhosComDeficiencia() + "%"),
                        List.of("% familiar com deficiência", dto.getPercentualFamiliarComDeficiencia() + "%"),
                        List.of("% sem trabalho", dto.getPercentualSemTrabalho() + "%")
                ), response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Perfil social");
        model.addAttribute("perfil", dto);
        return "beneficios/relatorios/perfil-social";
    }

    @GetMapping("/historico-beneficiario/{id}")
    public String historicoBeneficiario(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        model.addAttribute("tituloPagina", "Linha do tempo do beneficiário");
        model.addAttribute("beneficiarioId", id);
        model.addAttribute("linhaDoTempo", relatorioService.historicoBeneficiario(id));
        return "beneficios/relatorios/historico-beneficiario";
    }

    @GetMapping("/prazos-vencendo")
    public String prazosVencendo(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        int dias = filtro.getDias() == null ? 30 : filtro.getDias();
        List<PrazoVencendoDto> dados = relatorioService.prazosVencendo(dias);

        if (exportarSeSolicitado(filtro, "prazos_vencendo", dias + "dias", "Prazos vencendo",
                List.of("Nome", "CPF", "Prazo final", "Dias restantes"),
                dados.stream().map(d -> List.of(d.getNome(), cpfParaExportacao(d.getCpf()),
                        d.getPrazoFinalBeneficio().toString(), String.valueOf(d.getDiasRestantes()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Prazos vencendo");
        model.addAttribute("dias", dias);
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/prazos-vencendo";
    }

    @GetMapping("/inativos-recorrentes")
    public String inativosRecorrentes(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        int meses = filtro.getDias() == null ? 3 : filtro.getDias();
        List<InativoRecorrenteDto> dados = relatorioService.inativosRecorrentes(meses);

        if (exportarSeSolicitado(filtro, "inativos_recorrentes", meses + "meses", "Inativos recorrentes",
                List.of("Nome", "CPF", "Última retirada", "Meses sem retirar"),
                dados.stream().map(d -> List.of(d.getNome(), cpfParaExportacao(d.getCpf()),
                        d.getUltimaRetirada() == null ? "nunca" : d.getUltimaRetirada().toString(), String.valueOf(d.getMesesSemRetirar()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Inativos recorrentes");
        model.addAttribute("meses", meses);
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/inativos-recorrentes";
    }

    @GetMapping("/retiradas-por-terceiros")
    public String retiradasPorTerceiros(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        YearMonth inicio = parseMes(filtro.getMesInicio(), YearMonth.now());
        YearMonth fim = parseMes(filtro.getMesFim(), YearMonth.now());
        List<RetiradaPorTerceiroDto> dados = relatorioService.retiradasPorTerceiros(inicio, fim);

        if (exportarSeSolicitado(filtro, "retiradas_por_terceiros", inicio + "_a_" + fim, "Por terceiros",
                List.of("Tipo de retirante", "Quantidade"),
                dados.stream().map(d -> List.of(d.getTipoRetirante().getDescricao(), String.valueOf(d.getQuantidade()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Retiradas por terceiros");
        model.addAttribute("filtro", filtro);
        model.addAttribute("dados", dados);
        return "beneficios/relatorios/retiradas-por-terceiros";
    }

    @GetMapping("/confirmacoes")
    public String confirmacoes(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        LocalDateTime inicio = (filtro.getDataInicio() != null && !filtro.getDataInicio().isBlank()
                ? LocalDate.parse(filtro.getDataInicio()) : LocalDate.now().minusDays(30)).atStartOfDay();
        LocalDateTime fim = (filtro.getDataFim() != null && !filtro.getDataFim().isBlank()
                ? LocalDate.parse(filtro.getDataFim()) : LocalDate.now()).atTime(23, 59, 59);

        List<ConfirmacaoRelatorioDto> dados = relatorioService.confirmacoes(inicio, fim, filtro.getCodigo());

        if (exportarSeSolicitado(filtro, "confirmacoes", inicio.toLocalDate() + "_a_" + fim.toLocalDate(), "Confirmações",
                List.of("Data/hora", "Código", "Severidade", "Retirada", "Beneficiário", "Confirmado por", "Justificativa"),
                dados.stream().map(d -> List.of(d.getConfirmadoEm().toString(), d.getCodigoAviso(), d.getSeveridade().name(),
                        String.valueOf(d.getRetiradaId()), nvl(d.getBeneficiarioNome()), d.getConfirmadoPor(), nvl(d.getJustificativa()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Avisos confirmados (conformidade)");
        model.addAttribute("filtro", filtro);
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/confirmacoes";
    }

    @GetMapping("/voluntarios")
    public String voluntarios(RelatorioFiltro filtro, Model model, HttpServletResponse response) throws IOException {
        List<VoluntarioAnaliticoDto> dados = relatorioService.voluntariosAnalitico(filtro.getAtivo(), filtro.getAreaAtuacao());

        if (exportarSeSolicitado(filtro, "voluntarios", LocalDate.now().toString(), "Voluntários",
                List.of("Nome", "Área de atuação", "Setor", "Padrinho?", "Ativo?", "Entregas totais"),
                dados.stream().map(d -> List.of(d.getNome(), nvl(d.getAreaAtuacao()), nvl(d.getSetor()),
                        d.isPadrinho() ? "Sim" : "Não", d.isAtivo() ? "Sim" : "Não", String.valueOf(d.getEntregasTotal()))).toList(),
                response)) {
            return null;
        }

        model.addAttribute("tituloPagina", "Voluntários (analítico)");
        model.addAttribute("filtro", filtro);
        model.addAttribute("pagina", paginar(dados, filtro.getPagina()));
        return "beneficios/relatorios/voluntarios";
    }

    // ---- helpers ----

    private boolean exportarSeSolicitado(RelatorioFiltro filtro, String nomeRelatorio, String periodo, String nomeAba,
                                          List<String> cabecalhos, List<List<String>> linhas,
                                          HttpServletResponse response) throws IOException {
        if (filtro.getFormato() == null || filtro.getFormato().isBlank()) {
            return false;
        }
        String timestamp = LocalDateTime.now().format(FORMATO_ARQUIVO);
        String nomeArquivo = nomeRelatorio + "_" + periodo.replace("/", "-") + "_" + timestamp;

        ExportadorRelatorio exportador = "xlsx".equalsIgnoreCase(filtro.getFormato()) ? exportadorXlsx : exportadorCsv;
        exportador.exportar(nomeArquivo, nomeAba, cabecalhos, linhas, response);

        auditoriaService.registrar(AcaoAuditoria.EXPORTAR, "Relatorio", null,
                "Relatório '" + nomeRelatorio + "' exportado em " + filtro.getFormato().toUpperCase());
        return true;
    }

    private <T> Page<T> paginar(List<T> lista, int pagina) {
        int paginaValida = Math.max(pagina, 0);
        int tamanho = Math.min(TAMANHO_PAGINA_PADRAO, TAMANHO_PAGINA_MAXIMO);
        int total = lista.size();
        int inicio = Math.min(paginaValida * tamanho, total);
        int fim = Math.min(inicio + tamanho, total);
        return new PageImpl<>(new ArrayList<>(lista.subList(inicio, fim)), PageRequest.of(paginaValida, tamanho), total);
    }

    private YearMonth parseMes(String valor) {
        return parseMes(valor, YearMonth.now());
    }

    private YearMonth parseMes(String valor, YearMonth padrao) {
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        try {
            return YearMonth.parse(valor);
        } catch (Exception e) {
            return padrao;
        }
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }

    /** CPF completo só para ADM; demais perfis, mascarado (ver CLAUDE.md/LGPD). */
    private String cpfParaExportacao(String cpf) {
        return temRoleAdm() ? CpfUtils.formatar(cpf) : CpfUtils.mascarar(cpf);
    }

    private boolean temRoleAdm() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADM".equals(a.getAuthority()));
    }
}
