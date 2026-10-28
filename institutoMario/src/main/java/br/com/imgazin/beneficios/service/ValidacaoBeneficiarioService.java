package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.aviso.RegraAviso;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
