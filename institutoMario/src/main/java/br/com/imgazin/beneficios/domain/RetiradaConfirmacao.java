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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registra um aviso de confirmação exibido ao operador durante uma retirada
 * (ex.: "mesmo endereço de outro cadastro") e o fato de ele ter confirmado e
 * prosseguido mesmo assim — nunca um bloqueio, sempre um registro de decisão.
 * Não estende {@link Auditoria}: aqui quem cumpre esse papel é
 * {@code confirmadoPor}/{@code confirmadoEm}, já que o registro nasce e não
 * é editado depois.
 */
@Getter
@Setter
@Entity
@Table(name = "retirada_confirmacao")
public class RetiradaConfirmacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Retirada é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retirada_id", nullable = false)
    private RetiradaCesta retirada;

    @NotBlank(message = "Código do aviso é obrigatório")
    @Size(max = 60)
    @Column(name = "codigo_aviso", nullable = false, length = 60)
    private String codigoAviso;

    @NotNull(message = "Severidade é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "severidade", nullable = false, length = 20)
    private SeveridadeAviso severidade;

    @NotBlank(message = "Mensagem é obrigatória")
    @Column(name = "mensagem", nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @NotBlank(message = "Usuário que confirmou é obrigatório")
    @Size(max = 120)
    @Column(name = "confirmado_por", nullable = false, length = 120)
    private String confirmadoPor;

    @NotNull(message = "Data/hora da confirmação é obrigatória")
    @Column(name = "confirmado_em", nullable = false)
    private LocalDateTime confirmadoEm;

    @Column(name = "justificativa", columnDefinition = "TEXT")
    private String justificativa;
}
