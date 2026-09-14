package br.com.imgazin.beneficios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Trilha de auditoria do módulo Benefícios: quem consultou/alterou/exportou
 * qual registro, quando e com qual perfil — exigida pela LGPD para dado
 * sensível (CPF, endereço, renda). Referência polimórfica simples
 * ({@code entidade} + {@code entidadeId}), sem FK, de propósito: o alvo pode
 * ser qualquer entidade do módulo.
 */
@Getter
@Setter
@Entity
@Table(name = "beneficios_auditoria")
public class BeneficiosAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Entidade é obrigatória")
    @Size(max = 80)
    @Column(name = "entidade", nullable = false, length = 80)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @NotNull(message = "Ação é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "acao", nullable = false, length = 20)
    private AcaoAuditoria acao;

    @NotBlank(message = "Usuário é obrigatório")
    @Size(max = 120)
    @Column(name = "usuario", nullable = false, length = 120)
    private String usuario;

    @Size(max = 40)
    @Column(name = "perfil", length = 40)
    private String perfil;

    @Size(max = 45)
    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "dados_antes", columnDefinition = "TEXT")
    private String dadosAntes;

    @Column(name = "dados_depois", columnDefinition = "TEXT")
    private String dadosDepois;

    @NotNull(message = "Data/hora é obrigatória")
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;
}
