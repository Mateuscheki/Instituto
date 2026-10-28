package br.com.imgazin.beneficios.domain;

public enum RespostaSimNao {

    SIM("Sim"),
    NAO("Não"),
    NAO_INFORMADO("Não informado");

    private final String descricao;

    RespostaSimNao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
