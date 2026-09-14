package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegraRetiranteEBeneficiarioTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Test
    void geraAvisoQuandoCpfDoRetiranteEhOutroBeneficiarioAtivo() {
        RegraRetiranteEBeneficiario regra = new RegraRetiranteEBeneficiario(beneficiarioRepository);

        Beneficiario atual = new Beneficiario();
        atual.setId(1L);

        RetiradaForm form = new RetiradaForm();
        form.setRetiranteCpf("111.444.777-35");

        Beneficiario outro = new Beneficiario();
        outro.setId(2L);
        outro.setNome("Outro Beneficiário");
        outro.setAtivo(true);

        when(beneficiarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(outro));

        var avisos = regra.avaliar(new ContextoAvaliacaoRetirada(form, atual));

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("RETIRANTE_E_BENEFICIARIO");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("ATENCAO");
    }

    @Test
    void naoGeraAvisoQuandoORetiranteEhOProprioBeneficiario() {
        RegraRetiranteEBeneficiario regra = new RegraRetiranteEBeneficiario(beneficiarioRepository);

        Beneficiario atual = new Beneficiario();
        atual.setId(1L);

        RetiradaForm form = new RetiradaForm();
        form.setRetiranteCpf("111.444.777-35");

        when(beneficiarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(atual));

        assertThat(regra.avaliar(new ContextoAvaliacaoRetirada(form, atual))).isEmpty();
    }
}
