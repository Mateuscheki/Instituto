package br.com.imgazin.beneficios.exception;

/**
 * Dois atendentes tentaram registrar a retirada do mesmo beneficiário no
 * mesmo mês ao mesmo tempo; o índice único do banco (ver migration V1)
 * rejeitou o segundo INSERT. Convertido aqui num retorno de negócio em vez
 * de estourar como erro 500 (ver CLAUDE.md, Etapa 3: "convertendo em
 * retorno de negócio na tela").
 */
public class RetiradaConcorrenteException extends RuntimeException {

    public RetiradaConcorrenteException() {
        super("Outra pessoa já registrou uma retirada para este beneficiário neste mês nos últimos instantes.");
    }
}
