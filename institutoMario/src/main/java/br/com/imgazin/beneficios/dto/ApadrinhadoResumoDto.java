package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Uma linha na tela "apadrinhados" (GET /beneficios/padrinhos/{voluntarioId}/apadrinhados). */
@Data
@AllArgsConstructor
public class ApadrinhadoResumoDto {
    private Long beneficiarioId;
    private String nome;
    private String cpf;
    private boolean retirouMesAtual;
    private boolean ativo;
}
