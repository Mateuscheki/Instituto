package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.YearMonth;

@Data
@AllArgsConstructor
public class LinhaTempoBeneficiarioDto {
    private YearMonth mes;
    private String situacao; // RETIROU, NAO_RETIROU, CANCELADA
    private TipoRetirante tipoRetirante; // null quando NAO_RETIROU
}
