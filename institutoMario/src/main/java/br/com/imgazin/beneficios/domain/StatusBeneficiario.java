package br.com.imgazin.beneficios.domain;

/**
 * Situação do beneficiário em relação ao programa de cestas básicas.
 * {@code descricao} e {@code cssBadge} existem só para a view — nada de lógica
 * de negócio aqui, é puramente rótulo + classe CSS do badge de status.
 */
public enum StatusBeneficiario {

    APTO_MES_ATUAL("Apto no mês atual", "badge-sucesso"),
    RETIRADO_MES_ATUAL("Já retirou no mês atual", "badge-info"),
    AGUARDANDO_ANALISE("Aguardando análise", "badge-alerta"),
    SUSPENSO("Suspenso", "badge-perigo"),
    PRAZO_ENCERRADO("Prazo do benefício encerrado", "badge-perigo"),
    INATIVO("Inativo", "badge-neutro");

    private final String descricao;
    private final String cssBadge;

    StatusBeneficiario(String descricao, String cssBadge) {
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
