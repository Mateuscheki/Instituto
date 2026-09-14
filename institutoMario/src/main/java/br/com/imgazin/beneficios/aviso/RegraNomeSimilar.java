package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.util.TextoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NOME_SIMILAR (INFO): nome com similaridade > 0,85 (Levenshtein sobre texto
 * normalizado — sem acento, minúsculo) com outro cadastro ativo. Sem
 * extensão de similaridade de texto no MySQL (ao contrário do
 * {@code similarity} do Postgres), então a comparação é feita em memória.
 */
@Component
@RequiredArgsConstructor
public class RegraNomeSimilar implements RegraAviso {

    private static final double LIMIAR_SIMILARIDADE = 0.85;

    private final BeneficiarioRepository beneficiarioRepository;

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        String nome = form.getNome();
        if (nome == null || nome.isBlank()) {
            return List.of();
        }

        return beneficiarioRepository.findByAtivoTrue().stream()
                .filter(outro -> !outro.getId().equals(form.getId()))
                .filter(outro -> TextoUtils.similaridade(nome, outro.getNome()) > LIMIAR_SIMILARIDADE)
                .map(outro -> Aviso.info(
                        "NOME_SIMILAR",
                        "Nome parecido com um cadastro já existente: " + outro.getNome() + ".",
                        outro.getId()))
                .toList();
    }
}
