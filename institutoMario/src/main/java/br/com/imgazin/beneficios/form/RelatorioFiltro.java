package br.com.imgazin.beneficios.form;

import lombok.Data;

/**
 * Filtro único e flexível, reaproveitado por todos os relatórios (cada tela
 * só usa o subconjunto de campos que faz sentido para ela — ver
 * {@code fragments/filtroPeriodo.html}). Tudo String/wrapper e opcional,
 * mesmo padrão dos outros filtros do módulo.
 */
@Data
public class RelatorioFiltro {
    private String mes;          // relatórios de um mês só (pendentes, dashboard)
    private String mesInicio;    // relatórios de série (retiradas-mensais, por-padrinho...)
    private String mesFim;
    private String dataInicio;
    private String dataFim;
    private String cidade;
    private String bairro;
    private Long padrinhoId;
    private Integer dias;        // prazos-vencendo, inativos-recorrentes (em meses)
    private String agruparPor;   // por-regiao: bairro|cidade|uf
    private String codigo;       // confirmacoes: código do aviso
    private Boolean ativo;       // voluntarios
    private String areaAtuacao;  // voluntarios
    private String formato;      // csv|xlsx — export
    private int pagina = 0;
}
