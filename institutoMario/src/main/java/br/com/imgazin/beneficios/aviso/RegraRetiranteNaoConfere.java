package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.util.TextoUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RETIRANTE_NAO_CONFERE (ALTA): quando o retirante alega ser o próprio
 * beneficiário, o cônjuge ou a mãe, mas o nome digitado não bate com o
 * cadastrado. Só se aplica a esses três tipos — para padrinho/outro
 * familiar/terceiro autorizado o retirante É, por definição, outra pessoa.
 */
@Component
public class RegraRetiranteNaoConfere implements RegraAvisoRetirada {

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        Beneficiario beneficiario = contexto.getBeneficiario();
        RetiradaForm form = contexto.getForm();
        if (beneficiario == null || form.getTipoRetirante() == null) {
            return List.of();
        }

        TipoRetirante tipo = parsear(form.getTipoRetirante());
        if (tipo != TipoRetirante.PROPRIO && tipo != TipoRetirante.CONJUGE && tipo != TipoRetirante.MAE) {
            return List.of();
        }

        String nomeEsperado = switch (tipo) {
            case PROPRIO -> beneficiario.getNome();
            case CONJUGE -> beneficiario.getNomeConjuge();
            case MAE -> beneficiario.getNomeMae();
            default -> null;
        };

        String nomeInformado = form.getRetiranteNome();
        if (nomeInformado == null || nomeInformado.isBlank() || nomeEsperado == null) {
            return List.of();
        }

        boolean confere = TextoUtils.normalizarSemAcento(nomeEsperado).equals(TextoUtils.normalizarSemAcento(nomeInformado));
        if (confere) {
            return List.of();
        }

        return List.of(Aviso.alta("RETIRANTE_NAO_CONFERE",
                "O nome do retirante não confere com " + rotulo(tipo) + " cadastrado(a).",
                beneficiario.getId()));
    }

    private TipoRetirante parsear(String valor) {
        try {
            return TipoRetirante.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String rotulo(TipoRetirante tipo) {
        return switch (tipo) {
            case PROPRIO -> "o nome do beneficiário";
            case CONJUGE -> "o nome do cônjuge";
            case MAE -> "o nome da mãe";
            default -> "";
        };
    }
}
