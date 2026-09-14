package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PrazoVencendoDto {
    private Long beneficiarioId;
    private String nome;
    private String cpf;
    private LocalDate prazoFinalBeneficio;
    private long diasRestantes; // negativo = já vencido
}
