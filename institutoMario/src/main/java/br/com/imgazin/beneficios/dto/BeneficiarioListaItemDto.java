package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/** Uma linha da listagem paginada de beneficiários. */
@Data
@AllArgsConstructor
public class BeneficiarioListaItemDto {
    private Long id;
    private String cpf;
    private String nome;
    private String cidade;
    private String bairro;
    private StatusBeneficiario status;
    private LocalDate prazoFinalBeneficio;
    private String padrinhoNome;
    private boolean ativo;
}
