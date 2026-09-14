package br.com.imgazin.beneficios.service.export;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExportadorXlsxService implements ExportadorRelatorio {

    @Override
    public void exportar(String nomeArquivoBase, String nomeAba, List<String> cabecalhos,
                          List<List<String>> linhas, HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            String aba = nomeAba.length() > 31 ? nomeAba.substring(0, 31) : nomeAba;
            Sheet sheet = workbook.createSheet(aba);

            CellStyle estiloCabecalho = workbook.createCellStyle();
            Font fonteCabecalho = workbook.createFont();
            fonteCabecalho.setBold(true);
            estiloCabecalho.setFont(fonteCabecalho);
            estiloCabecalho.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            estiloCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row linhaCabecalho = sheet.createRow(0);
            for (int i = 0; i < cabecalhos.size(); i++) {
                Cell celula = linhaCabecalho.createCell(i);
                celula.setCellValue(cabecalhos.get(i));
                celula.setCellStyle(estiloCabecalho);
            }

            int indiceLinha = 1;
            for (List<String> linha : linhas) {
                Row row = sheet.createRow(indiceLinha++);
                for (int i = 0; i < linha.size(); i++) {
                    row.createCell(i).setCellValue(linha.get(i));
                }
            }

            for (int i = 0; i < cabecalhos.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivoBase + ".xlsx\"");
            workbook.write(response.getOutputStream());
        }
    }
}
