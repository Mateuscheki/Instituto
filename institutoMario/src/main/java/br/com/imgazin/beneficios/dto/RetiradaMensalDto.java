package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetiradaMensalDto {
    private YearMonth mes;
    private long retiradas;
    private long cestas;
    private long beneficiariosDistintos;
    private double percentualAdesao; // preenchido pelo service (precisa do total de aptos daquele mês)
}
