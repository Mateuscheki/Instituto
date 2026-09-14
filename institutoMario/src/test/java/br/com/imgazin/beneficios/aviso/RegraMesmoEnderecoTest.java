package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.util.EnderecoUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegraMesmoEnderecoTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    private RegraMesmoEndereco regra;

    @Test
    void geraAvisoQuandoOutroBeneficiarioAtivoTemAMesmaChave() {
        regra = new RegraMesmoEndereco(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setCep("74000-000");
        form.setNumero("123");
        form.setComplemento("Casa A");

        Beneficiario outro = new Beneficiario();
        outro.setId(99L);
        outro.setNome("Outro Morador");

        String chave = EnderecoUtils.calcularChaveResidencia("74000-000", "123", "Casa A");
        when(beneficiarioRepository.findByEnderecoChaveResidenciaAndAtivoTrue(chave)).thenReturn(List.of(outro));

        List<Aviso> avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("MESMO_ENDERECO");
        assertThat(avisos.get(0).getReferenciaId()).isEqualTo(99L);
        assertThat(avisos.get(0).isExigeJustificativa()).isFalse();
    }

    @Test
    void naoGeraAvisoParaSiMesmoAoEditar() {
        regra = new RegraMesmoEndereco(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setId(5L);
        form.setCep("74000-000");
        form.setNumero("123");
        form.setComplemento(null);

        Beneficiario proprio = new Beneficiario();
        proprio.setId(5L);

        when(beneficiarioRepository.findByEnderecoChaveResidenciaAndAtivoTrue(any())).thenReturn(List.of(proprio));

        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoConsultaRepositorioSemEndereco() {
        regra = new RegraMesmoEndereco(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        assertThat(regra.avaliar(form)).isEmpty();
    }
}
