package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Detalhe completo de uma retirada — usado no comprovante e na tela de detalhe. */
@Data
public class RetiradaDetalheDto {
    private Long id;
    private Long beneficiarioId;
    private String beneficiarioNome;
    private String beneficiarioCpf;
    private LocalDate mesReferencia;
    private LocalDateTime dataRetirada;
    private Integer quantidadeCestas;
    private TipoRetirante tipoRetirante;
    private String retiranteNome;
    private String retiranteCpf;
    private String retiranteVinculo;
    private boolean retiradoPorPadrinho;
    private String padrinhoNome;
    private String voluntarioEntregaNome;
    private String registradoPor;
    private String observacao;
    private boolean cancelada;
    private String motivoCancelamento;
    private LocalDateTime canceladaEm;
    private String canceladaPor;
    private LocalDateTime criadoEm;
    private List<RetiradaConfirmacaoDto> confirmacoes;
}
