package br.com.imgazin.beneficios.exception;

/**
 * Único caso de negócio que realmente bloqueia o cadastro (ver CLAUDE.md do
 * módulo, Etapa 2): já existe outro beneficiário com este CPF. Carrega o id
 * do cadastro existente para o controller oferecer "abrir cadastro existente".
 */
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
