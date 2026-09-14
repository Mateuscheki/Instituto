package br.com.imgazin.beneficios.domain;

/** Nível de atenção de um aviso de confirmação exibido ao operador. */
public enum SeveridadeAviso {

    INFO("Informativo", "badge-info"),
    ATENCAO("Atenção", "badge-alerta"),
    ALTA("Alta", "badge-perigo");

    private final String descricao;
    private final String cssBadge;

    SeveridadeAviso(String descricao, String cssBadge) {
        this.descricao = descricao;
        this.cssBadge = cssBadge;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCssBadge() {
        return cssBadge;
    }
}
