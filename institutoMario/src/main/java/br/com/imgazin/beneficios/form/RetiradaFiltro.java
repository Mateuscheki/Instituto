package br.com.imgazin.beneficios.form;

import lombok.Data;

/** Filtros do histórico de retiradas (todos opcionais). */
@Data
public class RetiradaFiltro {
    private String mes;
    private String dataInicio;
    private String dataFim;
    private String cpf;
    private Long padrinhoId;
    private Long voluntarioId;
    private String tipoRetirante;
    private String cidade;
    private String bairro;
    private boolean incluirCanceladas;
}
