package br.com.imgazin.beneficios.form;

import lombok.Data;

/** Filtros da listagem paginada de beneficiários (todos opcionais). */
@Data
public class BeneficiarioFiltro {
    private String nome;
    private String cpf;
    private String status;
    private String cidade;
    private String bairro;
    private Long padrinhoId;
    private Boolean ativo;
    private Integer prazoVencendoEmDias;
}
