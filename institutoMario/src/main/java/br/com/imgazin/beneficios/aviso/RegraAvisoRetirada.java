package br.com.imgazin.beneficios.aviso;

import java.util.List;

/**
 * Mesmo padrão de {@link RegraAviso} (Etapa 2), só que para o contexto de
 * retirada — interface própria (em vez de genérica) para não mexer nas 9
 * regras de beneficiário já em produção. Implementações são
 * {@code @Component}, coletadas como {@code List<RegraAvisoRetirada>} e
 * executadas em cadeia por {@code ValidacaoRetiradaService}.
 */
public interface RegraAvisoRetirada {

    List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto);
}
