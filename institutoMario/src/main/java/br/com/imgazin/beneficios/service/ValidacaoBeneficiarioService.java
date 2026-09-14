package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.aviso.RegraAviso;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de avisos: executa todas as {@link RegraAviso} registradas como bean
 * em cadeia e concatena os avisos. Não decide o que fazer com eles — quem
 * decide é {@link BeneficiarioService} (e o fluxo do controller).
 */
@Service
@RequiredArgsConstructor
public class ValidacaoBeneficiarioService {

    private final List<RegraAviso> regras;

    public List<Aviso> avaliar(BeneficiarioForm form) {
        List<Aviso> avisos = new ArrayList<>();
        for (RegraAviso regra : regras) {
            avisos.addAll(regra.avaliar(form));
        }
        return avisos;
    }
}
