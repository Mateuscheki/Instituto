package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** PADRINHO_DIVERGENTE (INFO): padrinho informado difere do vinculado ao cadastro. */
@Component
public class RegraPadrinhoDivergente implements RegraAvisoRetirada {

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        Beneficiario beneficiario = contexto.getBeneficiario();
        String padrinhoIdForm = contexto.getForm().getPadrinhoId();
        if (beneficiario == null || padrinhoIdForm == null || padrinhoIdForm.isBlank()) {
            return List.of();
        }

        Long informado;
        try {
            informado = Long.valueOf(padrinhoIdForm.trim());
        } catch (NumberFormatException e) {
            return List.of();
        }

        Long vinculado = beneficiario.getPadrinho() == null ? null : beneficiario.getPadrinho().getId();
        if (Objects.equals(informado, vinculado)) {
            return List.of();
        }

        return List.of(Aviso.info("PADRINHO_DIVERGENTE",
                "O padrinho/madrinha informado é diferente do vinculado a este cadastro.",
                vinculado));
    }
}
