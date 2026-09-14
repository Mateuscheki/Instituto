package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/** O relatório mais operacional: quem está apto e ainda não retirou no mês. */
@Data
@AllArgsConstructor
public class PendenteDto {
    private Long beneficiarioId;
    private String nome;
    private String cpf;
    private String telefone;
    private String bairro;
    private String padrinhoNome;
    private LocalDate ultimaRetirada;
    private Long diasSemRetirar;
}
