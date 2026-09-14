package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.SeveridadeAviso;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** Um aviso confirmado, registrado junto a uma retirada (tabela retirada_confirmacao). */
@Data
@AllArgsConstructor
public class RetiradaConfirmacaoDto {
    private String codigoAviso;
    private SeveridadeAviso severidade;
    private String mensagem;
    private String confirmadoPor;
    private LocalDateTime confirmadoEm;
    private String justificativa;
}
