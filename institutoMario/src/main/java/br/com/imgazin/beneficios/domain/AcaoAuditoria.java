package br.com.imgazin.beneficios.domain;

/**
 * Tipo de ação registrada em {@link BeneficiosAuditoria}. Não veio explícito
 * na lista de enums do pedido, mas é exigido pela coluna {@code acao} de
 * {@code beneficios_auditoria} (CRIAR/EDITAR/CONSULTAR/CANCELAR/EXPORTAR).
 */
public enum AcaoAuditoria {
    CRIAR,
    EDITAR,
    CONSULTAR,
    CANCELAR,
    EXPORTAR
}
