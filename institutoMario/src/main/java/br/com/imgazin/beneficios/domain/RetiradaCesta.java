package br.com.imgazin.beneficios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import br.com.imgazin.beneficios.validation.Cpf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de uma retirada mensal de cesta básica.
 * <p>
 * A coluna gerada {@code mes_referencia_ativo} (ver a migration V1) existe só
 * no banco, para sustentar a constraint "no máximo 1 retirada ativa por
 * beneficiário/mês" — de propósito não é mapeada aqui.
 */
@Getter
@Setter
@Entity
@Table(name = "retirada_cesta")
public class RetiradaCesta extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Beneficiário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiario_id", nullable = false)
    private Beneficiario beneficiario;

    @NotNull(message = "Mês de referência é obrigatório")
    @Column(name = "mes_referencia", nullable = false)
    private LocalDate mesReferencia;

    @NotNull(message = "Data da retirada é obrigatória")
    @Column(name = "data_retirada", nullable = false)
    private LocalDateTime dataRetirada;

    @NotNull
    @Column(name = "quantidade_cestas", nullable = false)
    private Integer quantidadeCestas = 1;

    @NotNull(message = "Tipo de retirante é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_retirante", nullable = false, length = 25)
    private TipoRetirante tipoRetirante;

    @NotBlank(message = "Nome do retirante é obrigatório")
    @Size(max = 150)
    @Column(name = "retirante_nome", nullable = false, length = 150)
    private String retiranteNome;

    @Cpf(message = "CPF do retirante inválido")
    @Column(name = "retirante_cpf", length = 11)
    private String retiranteCpf;

    @Size(max = 120)
    @Column(name = "retirante_vinculo", length = 120)
    private String retiranteVinculo;

    @Column(name = "retirado_por_padrinho", nullable = false)
    private boolean retiradoPorPadrinho = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padrinho_id")
    private Voluntario padrinho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voluntario_entrega_id")
    private Voluntario voluntarioEntrega;

    @NotBlank(message = "Usuário que registrou a retirada é obrigatório")
    @Size(max = 120)
    @Column(name = "registrado_por", nullable = false, length = 120)
    private String registradoPor;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "cancelada", nullable = false)
    private boolean cancelada = false;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    @Size(max = 120)
    @Column(name = "cancelada_por", length = 120)
    private String canceladaPor;
}
