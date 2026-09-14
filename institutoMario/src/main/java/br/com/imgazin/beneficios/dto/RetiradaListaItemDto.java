package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Uma linha do histórico de retiradas (listagem paginada). */
@Data
@AllArgsConstructor
public class RetiradaListaItemDto {
    private Long id;
    private Long beneficiarioId;
    private String beneficiarioNome;
    private String beneficiarioCpf;
    private LocalDate mesReferencia;
    private LocalDateTime dataRetirada;
    private TipoRetirante tipoRetirante;
    private String retiranteNome;
    private boolean retiradoPorPadrinho;
    private String padrinhoNome;
    private boolean cancelada;
}
