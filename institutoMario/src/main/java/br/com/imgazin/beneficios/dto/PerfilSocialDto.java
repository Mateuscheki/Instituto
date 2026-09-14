package br.com.imgazin.beneficios.dto;

import lombok.Data;

import java.util.Map;

/** Agregados do público atendido — só de ativos. */
@Data
public class PerfilSocialDto {
    private long totalBeneficiarios;
    private double mediaPessoasResidencia;
    private double percentualComFilhos;
    private double mediaFilhos;
    private double percentualFilhosComDeficiencia;
    private double percentualFamiliarComDeficiencia;
    private double percentualSemTrabalho;
    private Map<String, Long> faixasEtarias;
}
