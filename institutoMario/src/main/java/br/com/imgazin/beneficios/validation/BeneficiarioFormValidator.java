package br.com.imgazin.beneficios.validation;

import br.com.imgazin.beneficios.domain.RespostaSimNao;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import br.com.imgazin.beneficios.util.EnderecoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Validação ESTRUTURAL do formulário — formato, não regra de negócio. Erros
 * daqui são sempre bloqueantes (a tela volta com {@code BindingResult} e o
 * que foi digitado preservado). Regra de negócio (CPF com dígito
 * verificador errado, duplicidade de endereço/nome/telefone etc.) é o motor
 * de avisos ({@link br.com.imgazin.beneficios.service.ValidacaoBeneficiarioService}),
 * nunca bloqueia por si só.
 * <p>
 * Duplicidade de CPF (o único caso de negócio que também bloqueia) é
 * verificada à parte, no {@code BeneficiarioService}, porque depende do
 * fluxo (criação x edição) e do que fazer com o resultado (link para o
 * cadastro existente) — não cabe num Validator genérico.
 */
@Component
@RequiredArgsConstructor
public class BeneficiarioFormValidator implements Validator {

    private final VoluntarioRepository voluntarioRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return BeneficiarioForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BeneficiarioForm form = (BeneficiarioForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nome", "campo.obrigatorio", "Campo obrigatório.");
        validarCpfObrigatorioEFormato(form.getCpf(), "cpf", errors);
        validarCpfFormatoSeInformado(form.getCpfConjuge(), "cpfConjuge", errors);

        validarData(form.getDataNascimento(), "dataNascimento", errors);
        validarData(form.getPrazoFinalBeneficio(), "prazoFinalBeneficio", errors);

        validarInteiroNaoNegativo(form.getPessoasResidencia(), "pessoasResidencia", errors);
        validarInteiroNaoNegativo(form.getQuantidadeFilhos(), "quantidadeFilhos", errors);
        validarRenda(form.getRendaFamiliar(), errors);

        validarRespostaSimNao(form.getTemFilhos(), "temFilhos", errors);
        validarRespostaSimNao(form.getFilhosComDeficiencia(), "filhosComDeficiencia", errors);
        validarRespostaSimNao(form.getFamiliarComDeficiencia(), "familiarComDeficiencia", errors);
        validarRespostaSimNao(form.getTemTrabalho(), "temTrabalho", errors);

        validarStatus(form.getStatus(), errors);
        validarPadrinho(form.getPadrinhoId(), errors);
        validarCep(form.getCep(), errors);
        validarUf(form.getUf(), errors);
        validarEmail(form.getEmail(), errors);
    }

    private void validarCpfObrigatorioEFormato(String cpf, String campo, Errors errors) {
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
        String normalizado = CpfUtils.normalizar(cpf);
        if (normalizado.length() != 11) {
            errors.rejectValue(campo, "cpf.formato.invalido", "CPF deve conter 11 dígitos.");
        }
    }

    private void validarData(String valor, String campo, Errors errors) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        try {
            LocalDate.parse(valor);
        } catch (Exception e) {
            errors.rejectValue(campo, "data.invalida", "Data inválida.");
        }
    }

    private void validarInteiroNaoNegativo(String valor, String campo, Errors errors) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        try {
            int numero = Integer.parseInt(valor.trim());
            if (numero < 0) {
                errors.rejectValue(campo, "numero.negativo", "Não pode ser negativo.");
            }
        } catch (NumberFormatException e) {
            errors.rejectValue(campo, "numero.invalido", "Deve ser um número válido.");
        }
    }

    private void validarRenda(String valor, Errors errors) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        try {
            BigDecimal renda = new BigDecimal(valor.trim().replace(".", "").replace(",", "."));
            if (renda.signum() < 0) {
                errors.rejectValue("rendaFamiliar", "numero.negativo", "Não pode ser negativo.");
            }
        } catch (NumberFormatException e) {
            errors.rejectValue("rendaFamiliar", "numero.invalido", "Deve ser um número válido.");
        }
    }

    private void validarRespostaSimNao(String valor, String campo, Errors errors) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        try {
            RespostaSimNao.valueOf(valor);
        } catch (IllegalArgumentException e) {
            errors.rejectValue(campo, "valor.invalido", "Valor inválido.");
        }
    }

    private void validarStatus(String valor, Errors errors) {
        if (valor == null || valor.isBlank()) {
            return; // BeneficiarioService aplica o padrão AGUARDANDO_ANALISE
        }
        try {
            StatusBeneficiario.valueOf(valor);
        } catch (IllegalArgumentException e) {
            errors.rejectValue("status", "status.invalido", "Status inválido.");
        }
    }

    private void validarPadrinho(String padrinhoId, Errors errors) {
        if (padrinhoId == null || padrinhoId.isBlank()) {
            return;
        }
        try {
            Long id = Long.valueOf(padrinhoId.trim());
            if (!voluntarioRepository.existsById(id)) {
                errors.rejectValue("padrinhoId", "padrinho.invalido", "Padrinho/madrinha inválido.");
            }
        } catch (NumberFormatException e) {
            errors.rejectValue("padrinhoId", "padrinho.invalido", "Padrinho/madrinha inválido.");
        }
    }

    private void validarCep(String cep, Errors errors) {
        if (cep == null || cep.isBlank()) {
            return;
        }
        if (EnderecoUtils.normalizarCep(cep).length() != 8) {
            errors.rejectValue("cep", "cep.invalido", "CEP inválido.");
        }
    }

    private void validarUf(String uf, Errors errors) {
        if (uf == null || uf.isBlank()) {
            return;
        }
        if (!uf.trim().matches("(?i)[A-Z]{2}")) {
            errors.rejectValue("uf", "uf.invalida", "UF inválida.");
        }
    }

    private void validarEmail(String email, Errors errors) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (!email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errors.rejectValue("email", "email.invalido", "E-mail inválido.");
        }
    }
}
