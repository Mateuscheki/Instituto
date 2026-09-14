package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.SituacaoElegibilidade;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Painel de elegibilidade exibido antes do formulário de retirada. */
@Data
public class ElegibilidadeDto {
    private Long beneficiarioId;
    private String nome;
    private String cpf;
    private String nomeConjuge;
    private String nomeMae;
    private StatusBeneficiario status;
    private LocalDate mesReferencia;
    private SituacaoElegibilidade situacao;
    private LocalDateTime ultimaRetiradaData;
    private TipoRetirante ultimaRetiradaTipoRetirante;
    private Long padrinhoVinculadoId;
    private String padrinhoVinculadoNome;
    private LocalDate prazoFinalBeneficio;
    private List<Aviso> avisosPreliminares;
}
