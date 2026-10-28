package br.com.imgazin.beneficios.form;

import lombok.Data;

@Data
public class RelatorioFiltro {
    private String mes;
    private String mesInicio;
    private String mesFim;
    private String dataInicio;
    private String dataFim;
    private String cidade;
    private String bairro;
    private Long padrinhoId;
    private Integer dias; // prazos-vencendo (dias), inativos-recorrentes (meses)
    private String agruparPor; // bairro|cidade|uf
    private String codigo;
    private Boolean ativo;
    private String areaAtuacao;
    private String formato;
    private int pagina = 0;
}
