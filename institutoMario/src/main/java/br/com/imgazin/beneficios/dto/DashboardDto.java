package br.com.imgazin.beneficios.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardDto {
    private String mes;
    private long beneficiariosAtivos;
    private long aptosNoMes;
    private long retiradasDoMes;
    private double percentualAdesao;
    private long pendentes;
    private long cestasEntregues;
    private long novosCadastros;
    private long beneficiosVencendo30Dias;
    private long voluntariosAtivos;
    private long padrinhosAtivos;

    private List<RetiradaMensalDto> serieRetiradasPorMes;
    private List<PorRegiaoDto> retiradasPorBairro;
    private Map<String, Long> distribuicaoTipoRetirante;
}
