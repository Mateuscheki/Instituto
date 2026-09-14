package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RegraPrazoProximoTest {

    private final RegraPrazoProximo regra = new RegraPrazoProximo();

    @Test
    void geraAvisoInfoParaPrazoDentroDe30Dias() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().plusDays(15).toString());

        var avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("PRAZO_PROXIMO");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("INFO");
    }

    @Test
    void geraAvisoParaPrazoHoje() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().toString());
        assertThat(regra.avaliar(form)).hasSize(1);
    }

    @Test
    void geraAvisoParaPrazoExatamenteEm30Dias() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().plusDays(30).toString());
        assertThat(regra.avaliar(form)).hasSize(1);
    }

    @Test
    void naoGeraAvisoParaPrazoAlemDe30Dias() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().plusDays(31).toString());
        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoGeraAvisoParaPrazoJaVencido() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().minusDays(1).toString());
        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoGeraAvisoSemPrazo() {
        assertThat(regra.avaliar(new BeneficiarioForm())).isEmpty();
    }
}
