package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoluntarioAnaliticoDto {
    private Long id;
    private String nome;
    private String areaAtuacao;
    private String setor;
    private boolean padrinho;
    private boolean ativo;
    private long entregasTotal;
}
