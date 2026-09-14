package br.com.imgazin.beneficios.validation;

import br.com.imgazin.beneficios.util.CpfUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            // Presença é responsabilidade de @NotBlank/@NotNull, não desta anotação.
            return true;
        }
        return CpfUtils.isValido(valor);
    }
}
