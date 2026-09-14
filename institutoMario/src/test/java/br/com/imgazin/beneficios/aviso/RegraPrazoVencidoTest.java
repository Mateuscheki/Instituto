package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RegraPrazoVencidoTest {

    private final RegraPrazoVencido regra = new RegraPrazoVencido();

    @Test
    void geraAvisoAtencaoParaPrazoNoPassado() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().minusDays(1).toString());

        var avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("PRAZO_VENCIDO");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("ATENCAO");
    }

    @Test
    void naoGeraAvisoParaPrazoFuturo() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().plusDays(10).toString());
        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoGeraAvisoParaPrazoHoje() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio(LocalDate.now().toString());
        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoGeraAvisoSemPrazo() {
        assertThat(regra.avaliar(new BeneficiarioForm())).isEmpty();
    }

    @Test
    void naoGeraAvisoParaDataMalFormatada() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setPrazoFinalBeneficio("data-invalida");
        assertThat(regra.avaliar(form)).isEmpty();
    }
}
