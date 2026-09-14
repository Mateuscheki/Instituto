package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PorVoluntarioDto {
    private Long voluntarioId;
    private String nome;
    private String areaAtuacao;
    private String setor;
    private long entregas;
}
