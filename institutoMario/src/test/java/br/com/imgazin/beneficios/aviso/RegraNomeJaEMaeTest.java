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
class RegraNomeJaEMaeTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Test
    void geraAvisoAltaQuandoNomeJaEMaeDeOutroCadastro() {
        RegraNomeJaEMae regra = new RegraNomeJaEMae(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setNome("Joana da Silva");

        Beneficiario filho = new Beneficiario();
        filho.setId(11L);
        filho.setNome("Filho Cadastrado");

        when(beneficiarioRepository.findByNomeMaeIgnoreCaseAndAtivoTrue("Joana da Silva")).thenReturn(List.of(filho));

        List<Aviso> avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("NOME_JA_E_MAE");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("ALTA");
    }

    @Test
    void naoGeraAvisoSemNome() {
        RegraNomeJaEMae regra = new RegraNomeJaEMae(beneficiarioRepository);
        assertThat(regra.avaliar(new BeneficiarioForm())).isEmpty();
    }
}
