package br.com.imgazin.beneficios.validation;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Validação estrutural do formulário de retirada — formato, não regra de
 * negócio (isso é o motor de avisos, ver {@link br.com.imgazin.beneficios.service.ValidacaoRetiradaService}).
 */
@Component
@RequiredArgsConstructor
public class RetiradaFormValidator implements Validator {

    private static final int MESES_RETROATIVOS_PERMITIDOS = 3;

    private final VoluntarioRepository voluntarioRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RetiradaForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RetiradaForm form = (RetiradaForm) target;

        validarCpfObrigatorio(form.getCpfBeneficiario(), "cpfBeneficiario", errors);
        validarMesReferencia(form.getMesReferencia(), errors);
        validarDataHora(form.getDataRetirada(), errors);
        validarQuantidadeCestas(form.getQuantidadeCestas(), errors);
        TipoRetirante tipoRetirante = validarTipoRetirante(form.getTipoRetirante(), errors);

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "retiranteNome", "campo.obrigatorio", "Campo obrigatório.");
        validarCpfFormatoSeInformado(form.getRetiranteCpf(), "retiranteCpf", errors);

        if (tipoRetirante == TipoRetirante.OUTRO_FAMILIAR || tipoRetirante == TipoRetirante.TERCEIRO_AUTORIZADO) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "retiranteVinculo", "campo.obrigatorio", "Campo obrigatório.");
        }

        validarVoluntario(form.getPadrinhoId(), "padrinhoId", errors);
        validarVoluntario(form.getVoluntarioEntregaId(), "voluntarioEntregaId", errors);
    }

    private void validarCpfObrigatorio(String cpf, String campo, Errors errors) {
        if (cpf == null || cpf.isBlank()) {
            errors.rejectValue(campo, "campo.obrigatorio", "Campo obrigatório.");
            return;
        }
        validarCpfFormatoSeInformado(cpf, campo, errors);
    }

    private void validarCpfFormatoSeInformado(String cpf, String campo, Errors errors) {
        if (cpf == null || cpf.isBlank()) {
            return;
        }
        if (CpfUtils.normalizar(cpf).length() != 11) {
            errors.rejectValue(campo, "cpf.formato.invalido", "CPF deve conter 11 dígitos.");
        }
    }

    private void validarMesReferencia(String valor, Errors errors) {
        if (valor == null || valor.isBlank()) {
            errors.rejectValue("mesReferencia", "campo.obrigatorio", "Campo obrigatório.");
            return;
        }
        try {
            YearMonth mes = YearMonth.parse(valor);
            YearMonth atual = YearMonth.now();
            YearMonth limiteRetroativo = atual.minusMonths(MESES_RETROATIVOS_PERMITIDOS);
            if (mes.isAfter(atual) || mes.isBefore(limiteRetroativo)) {
                errors.rejectValue("mesReferencia", "mes.fora.do.limite",
                        "O mês de referência deve estar entre " + limiteRetroativo + " e " + atual + ".");
            }
        } catch (DateTimeParseException e) {
            errors.rejectValue("mesReferencia", "data.invalida", "Data inválida.");
        }
    }

    private void validarDataHora(String valor, Errors errors) {
        if (valor == null || valor.isBlank()) {
            errors.rejectValue("dataRetirada", "campo.obrigatorio", "Campo obrigatório.");
            return;
        }
        try {
            LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            errors.rejectValue("dataRetirada", "data.invalida", "Data inválida.");
        }
    }

    private void validarQuantidadeCestas(String valor, Errors errors) {
        if (valor == null || valor.isBlank()) {
            errors.rejectValue("quantidadeCestas", "campo.obrigatorio", "Campo obrigatório.");
            return;
        }
        try {
            int quantidade = Integer.parseInt(valor.trim());
            if (quantidade <= 0) {
                errors.rejectValue("quantidadeCestas", "numero.invalido", "Deve ser maior que zero.");
            }
        } catch (NumberFormatException e) {
            errors.rejectValue("quantidadeCestas", "numero.invalido", "Deve ser um número válido.");
        }
    }

    private TipoRetirante validarTipoRetirante(String valor, Errors errors) {
        if (valor == null || valor.isBlank()) {
            errors.rejectValue("tipoRetirante", "campo.obrigatorio", "Campo obrigatório.");
            return null;
        }
        try {
            return TipoRetirante.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            errors.rejectValue("tipoRetirante", "valor.invalido", "Valor inválido.");
            return null;
        }
    }

    private void validarVoluntario(String id, String campo, Errors errors) {
        if (id == null || id.isBlank()) {
            return;
        }
        try {
            Long valor = Long.valueOf(id.trim());
            if (!voluntarioRepository.existsById(valor)) {
                errors.rejectValue(campo, "valor.invalido", "Valor inválido.");
            }
        } catch (NumberFormatException e) {
            errors.rejectValue(campo, "valor.invalido", "Valor inválido.");
        }
    }
}
