package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Uma linha do histórico de retiradas exibido na ficha do beneficiário. */
@Data
@AllArgsConstructor
public class RetiradaResumoDto {
    private Long id;
    private LocalDate mesReferencia;
    private LocalDateTime dataRetirada;
    private TipoRetirante tipoRetirante;
    private String retiranteNome;
    private boolean cancelada;
}
