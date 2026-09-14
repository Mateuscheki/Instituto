package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.YearMonth;

/** Uma linha de "entregas por mês" na tela de atuação do voluntário. */
@Data
@AllArgsConstructor
public class EntregaMesDto {
    private YearMonth mes;
    private long quantidade;
}
