package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.form.VoluntarioForm;
import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidacaoVoluntarioService {

    public List<Aviso> avaliar(VoluntarioForm form) {
        String cpf = form.getCpf();
        if (cpf == null || cpf.isBlank()) {
            return List.of();
        }
        String normalizado = CpfUtils.normalizar(cpf);
        if (normalizado.length() != 11 || CpfUtils.isValido(normalizado)) {
            return List.of();
        }
        return List.of(Aviso.alta("CPF_DIGITO_INVALIDO",
                "O CPF informado (" + CpfUtils.formatar(normalizado) + ") não passa na validação do dígito verificador.",
                null));
    }
}
