package br.com.imgazin.beneficios.domain;

/**
 * Resposta de perguntas de triagem social que admitem "não sei/não informado"
 * além de sim/não (tem filhos, tem trabalho, familiar com deficiência etc.).
 */
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
