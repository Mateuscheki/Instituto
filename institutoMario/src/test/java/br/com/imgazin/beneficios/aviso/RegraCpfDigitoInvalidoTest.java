package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegraCpfDigitoInvalidoTest {

    private final RegraCpfDigitoInvalido regra = new RegraCpfDigitoInvalido();

    @Test
    void geraAvisoAltaParaCpfComDigitoErrado() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setCpf("111.444.777-30"); // base válida 111.444.777-35, dígito trocado

        var avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("CPF_DIGITO_INVALIDO");
        assertThat(avisos.get(0).isExigeJustificativa()).isTrue();
    }

    @Test
    void naoGeraAvisoParaCpfValido() {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setCpf("111.444.777-35");

        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoGeraAvisoParaCpfEmBranco() {
        BeneficiarioForm form = new BeneficiarioForm();
        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoGeraAvisoParaCpfComTamanhoErrado() {
        // erro de formulário (bloqueante), não aviso — não é responsabilidade desta regra
        BeneficiarioForm form = new BeneficiarioForm();
        form.setCpf("123");
        assertThat(regra.avaliar(form)).isEmpty();
    }
}
