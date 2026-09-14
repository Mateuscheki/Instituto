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
class RegraMesmoNomeMaeTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Test
    void geraAvisoQuandoOutroCadastroTemAMesmaMae() {
        RegraMesmoNomeMae regra = new RegraMesmoNomeMae(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setNomeMae("Joana da Silva");

        Beneficiario irmao = new Beneficiario();
        irmao.setId(7L);
        irmao.setNome("Irmão Cadastrado");

        when(beneficiarioRepository.findByNomeMaeIgnoreCaseAndAtivoTrue("Joana da Silva")).thenReturn(List.of(irmao));

        List<Aviso> avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("MESMO_NOME_MAE");
        assertThat(avisos.get(0).getReferenciaId()).isEqualTo(7L);
    }

    @Test
    void naoGeraAvisoSemNomeDaMae() {
        RegraMesmoNomeMae regra = new RegraMesmoNomeMae(beneficiarioRepository);
        BeneficiarioForm form = new BeneficiarioForm();
        assertThat(regra.avaliar(form)).isEmpty();
    }
}
