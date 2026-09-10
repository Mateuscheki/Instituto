package edu.unialfa.institutoMario.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Registro de uma ação relevante realizada no sistema (criação, edição, exclusão de
 * dados ou eventos de autenticação). Não guarda o conteúdo alterado, apenas quem fez
 * o quê, quando e em qual registro — evita vazar dados sensíveis (ex.: senha) no log.
 */
@Entity
@Table(name = "log_auditoria")
@Data
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    /** Id do usuário autenticado que realizou a ação; nulo quando não há usuário autenticado (ex.: falha de login). */
    private Long usuarioId;

    /** Nome/identificador do usuário no momento da ação (snapshot, não segue alterações futuras). */
    @Column(length = 150)
    private String usuarioNome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAcao acao;

    /** Nome da entidade/recurso afetado, ex.: "Usuario", "Prova", "Turma". */
    @Column(length = 60)
    private String entidade;

    /** Id do registro afetado, quando aplicável. */
    private Long entidadeId;

    @Column(length = 500)
    private String descricao;

    @Column(length = 45)
    private String ip;
}
