package br.com.imgazin.beneficios.service.export;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV em UTF-8 com BOM e separador `;` — é isso que faz o Excel em pt-BR
 * abrir com a acentuação correta (ver CLAUDE.md, Etapa 5).
 */
@Service
public class ExportadorCsvService implements ExportadorRelatorio {

    private static final byte[] BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Override
    public void exportar(String nomeArquivoBase, String nomeAba, List<String> cabecalhos,
                          List<List<String>> linhas, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivoBase + ".csv\"");

        response.getOutputStream().write(BOM_UTF8);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.println(String.join(";", cabecalhos.stream().map(this::escapar).toList()));
            for (List<String> linha : linhas) {
                writer.println(linha.stream().map(this::escapar).reduce((a, b) -> a + ";" + b).orElse(""));
            }
            writer.flush();
        }
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
