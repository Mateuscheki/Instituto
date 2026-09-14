package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.form.VoluntarioForm;
import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * "Mesma validação por aviso" do CPF do beneficiário (CLAUDE.md, Etapa 4):
 * CPF com 11 dígitos mas dígito verificador errado gera aviso ALTA, não
 * bloqueia. Só essa regra existe aqui — não precisou da arquitetura de
 * "motor de avisos" plugável das Etapas 2/3 (que existe para várias regras
 * coordenadas); reaproveita a mesma classe {@link Aviso} e o mesmo padrão de
 * tela de confirmação.
 */
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
