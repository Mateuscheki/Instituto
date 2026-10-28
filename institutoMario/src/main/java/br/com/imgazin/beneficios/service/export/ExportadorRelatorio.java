package br.com.imgazin.beneficios.service.export;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface ExportadorRelatorio {

    void exportar(String nomeArquivoBase, String nomeAba, List<String> cabecalhos,
                  List<List<String>> linhas, HttpServletResponse response) throws IOException;
}
