package br.com.imgazin.beneficios.exception;

public class CpfDuplicadoException extends RuntimeException {

    private final Long idExistente;

    public CpfDuplicadoException(Long idExistente) {
        super("Já existe um beneficiário cadastrado com este CPF.");
        this.idExistente = idExistente;
    }

    public Long getIdExistente() {
        return idExistente;
    }
}
