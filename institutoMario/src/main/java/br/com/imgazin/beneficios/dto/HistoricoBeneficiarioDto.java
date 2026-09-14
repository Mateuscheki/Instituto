package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** Histórico completo de retiradas de um beneficiário, com resumo (Etapa 3, entregável). */
@Data
public class HistoricoBeneficiarioDto {
    private Long beneficiarioId;
    private String beneficiarioNome;
    private String beneficiarioCpf;

    private long totalCestas;
    private int mesesConsecutivos;
    private int mesesSemRetirar;
    private Map<TipoRetirante, Long> distribuicaoPorTipoRetirante;

    private List<RetiradaResumoDto> retiradas;
}
