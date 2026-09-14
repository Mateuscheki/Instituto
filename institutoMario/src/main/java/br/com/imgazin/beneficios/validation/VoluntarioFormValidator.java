package br.com.imgazin.beneficios.validation;

import br.com.imgazin.beneficios.form.VoluntarioForm;
import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/** Validação estrutural (formato) do cadastro de voluntário — CPF é opcional aqui. */
@Component
public class VoluntarioFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return VoluntarioForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        VoluntarioForm form = (VoluntarioForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nome", "campo.obrigatorio", "Campo obrigatório.");

        String cpf = form.getCpf();
        if (cpf != null && !cpf.isBlank() && CpfUtils.normalizar(cpf).length() != 11) {
            errors.rejectValue("cpf", "cpf.formato.invalido", "CPF deve conter 11 dígitos.");
        }

        String email = form.getEmail();
        if (email != null && !email.isBlank() && !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errors.rejectValue("email", "email.invalido", "E-mail inválido.");
        }
    }
}
