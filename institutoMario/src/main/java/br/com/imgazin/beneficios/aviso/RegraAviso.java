package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;

import java.util.List;

/**
 * Uma regra do motor de avisos. Cada implementação é um {@code @Component}
 * próprio, coletadas automaticamente pelo Spring como {@code List<RegraAviso>}
 * e executadas em cadeia por {@code ValidacaoBeneficiarioService} — nunca
 * lançam exceção para "bloquear": no máximo devolvem uma lista vazia.
 */
public interface RegraAviso {

    List<Aviso> avaliar(BeneficiarioForm form);
}
