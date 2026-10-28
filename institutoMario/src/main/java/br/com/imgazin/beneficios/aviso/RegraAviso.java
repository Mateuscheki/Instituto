package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;

import java.util.List;

public interface RegraAviso {

    List<Aviso> avaliar(BeneficiarioForm form);
}
