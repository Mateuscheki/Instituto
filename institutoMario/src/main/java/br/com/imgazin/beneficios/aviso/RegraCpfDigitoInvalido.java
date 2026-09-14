package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CPF_DIGITO_INVALIDO (ALTA): o CPF tem 11 dígitos mas reprova no dígito
 * verificador. Não bloqueia — existe público real com documento irregular
 * (CLAUDE.md do módulo) — mas exige confirmação explícita.
 */
@Component
public class RegraCpfDigitoInvalido implements RegraAviso {

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        String cpf = form.getCpf();
        if (cpf == null || cpf.isBlank()) {
            return List.of();
        }
        String normalizado = CpfUtils.normalizar(cpf);
        if (normalizado.length() != 11) {
            // Tamanho errado é erro de formulário (bloqueante), não aviso — ver BeneficiarioFormValidator.
            return List.of();
        }
        if (CpfUtils.isValido(normalizado)) {
            return List.of();
        }
        return List.of(Aviso.alta(
                "CPF_DIGITO_INVALIDO",
                "O CPF informado (" + CpfUtils.formatar(normalizado) + ") não passa na validação do dígito verificador.",
                null
        ));
    }
}
