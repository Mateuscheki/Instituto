package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import org.springframework.stereotype.Component;

import java.util.List;

/** BENEFICIARIO_SUSPENSO (ALTA). */
@Component
public class RegraBeneficiarioSuspenso implements RegraAvisoRetirada {

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        Beneficiario beneficiario = contexto.getBeneficiario();
        if (beneficiario == null || beneficiario.getStatus() != StatusBeneficiario.SUSPENSO) {
            return List.of();
        }
        return List.of(Aviso.alta("BENEFICIARIO_SUSPENSO", "O beneficiário está com status SUSPENSO.", beneficiario.getId()));
    }
}
