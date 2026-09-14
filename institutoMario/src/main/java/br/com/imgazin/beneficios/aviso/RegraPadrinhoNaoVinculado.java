package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.form.RetiradaForm;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PADRINHO_NAO_VINCULADO (ATENCAO) — citada em prosa nas "Regras automáticas"
 * do CLAUDE.md (não veio na tabela de avisos, mas é claramente um aviso de
 * confirmação, não bloqueio): tipo de retirante é PADRINHO, mas não foi
 * informado nenhum e o beneficiário também não tem um vinculado.
 */
@Component
public class RegraPadrinhoNaoVinculado implements RegraAvisoRetirada {

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        RetiradaForm form = contexto.getForm();
        if (form.getTipoRetirante() == null) {
            return List.of();
        }

        TipoRetirante tipo;
        try {
            tipo = TipoRetirante.valueOf(form.getTipoRetirante());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        if (tipo != TipoRetirante.PADRINHO) {
            return List.of();
        }

        boolean informouPadrinho = form.getPadrinhoId() != null && !form.getPadrinhoId().isBlank();
        boolean temVinculado = contexto.getBeneficiario() != null && contexto.getBeneficiario().getPadrinho() != null;

        if (informouPadrinho || temVinculado) {
            return List.of();
        }

        Long referenciaId = contexto.getBeneficiario() == null ? null : contexto.getBeneficiario().getId();
        return List.of(Aviso.atencao("PADRINHO_NAO_VINCULADO",
                "O tipo de retirante é padrinho/madrinha, mas nenhum foi informado nem está vinculado ao beneficiário.",
                referenciaId));
    }
}
