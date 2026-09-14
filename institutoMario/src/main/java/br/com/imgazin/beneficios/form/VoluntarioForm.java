package br.com.imgazin.beneficios.form;

import lombok.Data;

/** Mesmo padrão dos outros forms do módulo: tudo String, nunca perde o digitado. */
@Data
public class VoluntarioForm {
    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String areaAtuacao;
    private String setor;
    private String padrinho; // checkbox: "true"/"false"/null
    private String disponibilidade;
    private String observacao;
}
