package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegraTelefoneDuplicadoTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Test
    void geraAvisoInfoQuandoTelefoneJaCadastrado() {
        RegraTelefoneDuplicado regra = new RegraTelefoneDuplicado(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setTelefone("(62) 99999-9999");

        Beneficiario outro = new Beneficiario();
        outro.setId(21L);
        outro.setNome("Outro Contato");

        when(beneficiarioRepository.findByTelefoneAndAtivoTrue("(62) 99999-9999")).thenReturn(List.of(outro));

        List<Aviso> avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("TELEFONE_DUPLICADO");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("INFO");
        assertThat(avisos.get(0).isExigeJustificativa()).isFalse();
    }

    @Test
    void naoGeraAvisoSemTelefone() {
        RegraTelefoneDuplicado regra = new RegraTelefoneDuplicado(beneficiarioRepository);
        assertThat(regra.avaliar(new BeneficiarioForm())).isEmpty();
    }
}
