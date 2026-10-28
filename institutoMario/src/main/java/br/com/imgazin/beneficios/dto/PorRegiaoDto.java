package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PorRegiaoDto {
    private String regiao;
    private long beneficiarios;
    private long retiradas;
}
