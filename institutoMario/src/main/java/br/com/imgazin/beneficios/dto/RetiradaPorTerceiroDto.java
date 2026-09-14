package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RetiradaPorTerceiroDto {
    private TipoRetirante tipoRetirante;
    private long quantidade;
}
