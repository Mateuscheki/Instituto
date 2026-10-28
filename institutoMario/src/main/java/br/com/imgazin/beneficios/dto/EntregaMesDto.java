package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.YearMonth;

@Data
@AllArgsConstructor
public class EntregaMesDto {
    private YearMonth mes;
    private long quantidade;
}
