package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** {@code regiao} é o valor de bairro, cidade ou UF conforme o agrupamento escolhido. */
@Data
@AllArgsConstructor
public class PorRegiaoDto {
    private String regiao;
    private long beneficiarios;
    private long retiradas;
}
