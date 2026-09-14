package br.com.imgazin.beneficios.domain;

/** Quem efetivamente retirou a cesta em nome do beneficiário. */
public enum TipoRetirante {

    PROPRIO("O próprio beneficiário"),
    CONJUGE("Cônjuge"),
    MAE("Mãe"),
    PADRINHO("Padrinho/madrinha"),
    OUTRO_FAMILIAR("Outro familiar"),
    TERCEIRO_AUTORIZADO("Terceiro autorizado");

    private final String descricao;

    TipoRetirante(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
