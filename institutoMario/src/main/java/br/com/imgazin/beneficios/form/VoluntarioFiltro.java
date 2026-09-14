package br.com.imgazin.beneficios.form;

import lombok.Data;

@Data
public class VoluntarioFiltro {
    private String nome;
    private String cpf;
    private String areaAtuacao;
    private String setor;
    private Boolean isPadrinho;
    private Boolean ativo;
}
