package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.SeveridadeAviso;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** Relatório de conformidade: todo aviso confirmado no período. */
@Data
@AllArgsConstructor
public class ConfirmacaoRelatorioDto {
    private LocalDateTime confirmadoEm;
    private String codigoAviso;
    private SeveridadeAviso severidade;
    private Long retiradaId;
    private String beneficiarioNome;
    private String confirmadoPor;
    private String justificativa;
}
