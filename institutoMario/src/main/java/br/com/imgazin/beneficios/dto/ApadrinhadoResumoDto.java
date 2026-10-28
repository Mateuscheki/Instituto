package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApadrinhadoResumoDto {
    private Long beneficiarioId;
    private String nome;
    private String cpf;
    private boolean retirouMesAtual;
    private boolean ativo;
}
