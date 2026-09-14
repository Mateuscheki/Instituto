package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegraCpfJaEConjugeTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Test
    void geraAvisoAltaQuandoCpfJaEConjugeDeOutroCadastro() {
        RegraCpfJaEConjuge regra = new RegraCpfJaEConjuge(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setCpf("111.444.777-35");
        form.setNome("Fulano Teste");

        Beneficiario outro = new Beneficiario();
        outro.setId(3L);
        outro.setNome("Cadastro Titular");

        when(beneficiarioRepository.findByCpfConjugeAndAtivoTrue("11144477735")).thenReturn(List.of(outro));
        lenient().when(beneficiarioRepository.findByNomeConjugeIgnoreCaseAndAtivoTrue("Fulano Teste")).thenReturn(List.of());

        List<Aviso> avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("CPF_JA_E_CONJUGE");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("ALTA");
    }

    @Test
    void naoDuplicaAvisoQuandoCpfENomeApontamParaOMesmoCadastro() {
        RegraCpfJaEConjuge regra = new RegraCpfJaEConjuge(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setCpf("111.444.777-35");
        form.setNome("Fulano Teste");

        Beneficiario outroPorCpf = new Beneficiario();
        outroPorCpf.setId(3L);
        outroPorCpf.setNome("Cadastro Titular");

        Beneficiario outroPorNomeMesmoId = new Beneficiario();
        outroPorNomeMesmoId.setId(3L);
        outroPorNomeMesmoId.setNome("Cadastro Titular");

        when(beneficiarioRepository.findByCpfConjugeAndAtivoTrue("11144477735")).thenReturn(List.of(outroPorCpf));
        when(beneficiarioRepository.findByNomeConjugeIgnoreCaseAndAtivoTrue("Fulano Teste")).thenReturn(List.of(outroPorNomeMesmoId));

        assertThat(regra.avaliar(form)).hasSize(1);
    }
}
