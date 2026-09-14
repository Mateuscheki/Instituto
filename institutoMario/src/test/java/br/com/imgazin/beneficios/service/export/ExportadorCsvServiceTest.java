package br.com.imgazin.beneficios.service.export;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Critério de aceite: "CSV exportado abre no Excel com acentuação correta". */
class ExportadorCsvServiceTest {

    private final ExportadorCsvService exportador = new ExportadorCsvService();

    @Test
    void geraCsvComBomUtf8SeparadorPontoEVirgulaEAcentuacaoPreservada() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        exportador.exportar("relatorio_teste", "Aba", List.of("Nome", "Bairro"),
                List.of(List.of("João da Conceição", "São José")), response);

        byte[] conteudo = response.getContentAsByteArray();

        // BOM UTF-8 (0xEF 0xBB 0xBF) logo no início — é isso que faz o Excel pt-BR abrir certo
        assertThat(conteudo[0]).isEqualTo((byte) 0xEF);
        assertThat(conteudo[1]).isEqualTo((byte) 0xBB);
        assertThat(conteudo[2]).isEqualTo((byte) 0xBF);

        String texto = new String(conteudo, 3, conteudo.length - 3, StandardCharsets.UTF_8);
        assertThat(texto).contains("Nome;Bairro");
        assertThat(texto).contains("João da Conceição;São José");

        assertThat(response.getContentType()).contains("text/csv");
        assertThat(response.getHeader("Content-Disposition")).contains("relatorio_teste.csv");
    }

    @Test
    void escapaValorComPontoEVirguraOuAspas() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        exportador.exportar("teste", "Aba", List.of("Campo"),
                List.of(List.of("Valor; com ponto e vírgula"), List.of("Valor com \"aspas\"")), response);

        String texto = new String(response.getContentAsByteArray(), 3, response.getContentAsByteArray().length - 3, StandardCharsets.UTF_8);
        assertThat(texto).contains("\"Valor; com ponto e vírgula\"");
        assertThat(texto).contains("\"Valor com \"\"aspas\"\"\"");
    }
}
