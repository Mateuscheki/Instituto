package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PorPadrinhoDto {
    private Long padrinhoId;
    private String nome;
    private long apadrinhados;
    private long retiradasPeriodo;
    private long retiradasPeloProprioPadrinho;
    private double taxaAdesao;
}
