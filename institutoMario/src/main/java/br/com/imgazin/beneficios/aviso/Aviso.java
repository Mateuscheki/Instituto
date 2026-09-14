package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.SeveridadeAviso;

/**
 * Um aviso de confirmação levantado pelo motor de avisos (ver
 * {@link RegraAviso}). Nunca bloqueia o cadastro — só ALTA exige que o
 * operador marque "confirmo e autorizo" e justifique antes de prosseguir.
 */
public class Aviso {

    private final String codigo;
    private final SeveridadeAviso severidade;
    private final String mensagem;
    private final Long referenciaId;
    private final boolean exigeJustificativa;

    public Aviso(String codigo, SeveridadeAviso severidade, String mensagem, Long referenciaId, boolean exigeJustificativa) {
        this.codigo = codigo;
        this.severidade = severidade;
        this.mensagem = mensagem;
        this.referenciaId = referenciaId;
        this.exigeJustificativa = exigeJustificativa;
    }

    public static Aviso alta(String codigo, String mensagem, Long referenciaId) {
        return new Aviso(codigo, SeveridadeAviso.ALTA, mensagem, referenciaId, true);
    }

    public static Aviso atencao(String codigo, String mensagem, Long referenciaId) {
        return new Aviso(codigo, SeveridadeAviso.ATENCAO, mensagem, referenciaId, false);
    }

    public static Aviso info(String codigo, String mensagem, Long referenciaId) {
        return new Aviso(codigo, SeveridadeAviso.INFO, mensagem, referenciaId, false);
    }

    public String getCodigo() {
        return codigo;
    }

    public SeveridadeAviso getSeveridade() {
        return severidade;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public boolean isExigeJustificativa() {
        return exigeJustificativa;
    }
}
