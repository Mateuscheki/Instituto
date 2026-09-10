package edu.unialfa.institutoMario.audit;

/**
 * Tipos de ação registrados no log de auditoria.
 */
public enum TipoAcao {
    CRIACAO,
    ATUALIZACAO,
    EXCLUSAO,
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    LOGOUT
}
