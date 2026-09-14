package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** PRAZO_ENCERRADO (ALTA): prazo final do benefício já passou. */
@Component
public class RegraPrazoEncerrado implements RegraAvisoRetirada {

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        Beneficiario beneficiario = contexto.getBeneficiario();
        if (beneficiario == null || beneficiario.getPrazoFinalBeneficio() == null) {
            return List.of();
        }
        if (!beneficiario.getPrazoFinalBeneficio().isBefore(LocalDate.now())) {
            return List.of();
        }
        return List.of(Aviso.alta("PRAZO_ENCERRADO", "O prazo final do benefício já passou.", beneficiario.getId()));
    }
}
