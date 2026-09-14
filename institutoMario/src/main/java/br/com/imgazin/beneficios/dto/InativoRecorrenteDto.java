package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class InativoRecorrenteDto {
    private Long beneficiarioId;
    private String nome;
    private String cpf;
    private LocalDate ultimaRetirada; // null = nunca retirou
    private long mesesSemRetirar;
}
