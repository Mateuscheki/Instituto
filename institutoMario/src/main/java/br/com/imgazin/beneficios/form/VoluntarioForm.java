package br.com.imgazin.beneficios.form;

import lombok.Data;

@Data
public class VoluntarioForm {
    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String areaAtuacao;
    private String setor;
    private String padrinho;
    private String disponibilidade;
    private String observacao;
}
