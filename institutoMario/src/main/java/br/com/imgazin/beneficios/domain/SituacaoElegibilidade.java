package br.com.imgazin.beneficios.domain;

/** Situação do beneficiário no painel de elegibilidade da tela de retirada. */
public enum SituacaoElegibilidade {

    ELEGIVEL("Elegível para retirar", "badge-sucesso"),
    JA_RETIROU("Já retirou neste mês", "badge-info"),
    PRAZO_ENCERRADO("Prazo do benefício encerrado", "badge-perigo"),
    SUSPENSO("Beneficiário suspenso", "badge-perigo"),
    INATIVO("Beneficiário inativo", "badge-neutro"),
    NAO_CADASTRADO("CPF não cadastrado", "badge-alerta");

    private final String descricao;
    private final String cssBadge;

    SituacaoElegibilidade(String descricao, String cssBadge) {
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
