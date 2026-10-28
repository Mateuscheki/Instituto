package br.com.imgazin.beneficios.exception;

public class RetiradaConcorrenteException extends RuntimeException {

    public RetiradaConcorrenteException() {
        super("Outra pessoa já registrou uma retirada para este beneficiário neste mês nos últimos instantes.");
    }
}
