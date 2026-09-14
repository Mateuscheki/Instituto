package br.com.imgazin.beneficios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Um período de vínculo entre um beneficiário e um padrinho/madrinha.
 * Nunca é apagado nem editado: trocar de padrinho encerra este registro
 * ({@code fim = hoje}) e cria um novo — assim relatórios antigos continuam
 * corretos (ver CLAUDE.md do módulo, Etapa 4).
 */
@Getter
@Setter
@Entity
@Table(name = "beneficiario_padrinho_historico")
public class BeneficiarioPadrinhoHistorico extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Beneficiário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiario_id", nullable = false)
    private Beneficiario beneficiario;

    @NotNull(message = "Voluntário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voluntario_id", nullable = false)
    private Voluntario voluntario;

    @NotNull(message = "Início é obrigatório")
    @Column(name = "inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "fim")
    private LocalDate fim;

    @Column(name = "motivo_fim", columnDefinition = "TEXT")
    private String motivoFim;
}
