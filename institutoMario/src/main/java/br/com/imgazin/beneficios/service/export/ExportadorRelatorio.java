package br.com.imgazin.beneficios.service.export;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Interface comum dos exportadores (CSV/XLSX). Recebe uma tabela genérica
 * (cabeçalhos + linhas de texto já formatadas) — quem monta essa tabela a
 * partir do DTO de cada relatório é o {@code RelatorioController}, então o
 * exportador não conhece nenhum DTO específico.
 */
public interface ExportadorRelatorio {

    void exportar(String nomeArquivoBase, String nomeAba, List<String> cabecalhos,
                  List<List<String>> linhas, HttpServletResponse response) throws IOException;
}
