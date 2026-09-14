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
class RegraNomeSimilarTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Test
    void geraAvisoInfoParaNomeMuitoParecido() {
        RegraNomeSimilar regra = new RegraNomeSimilar(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setNome("Joao da Silva Santos");

        Beneficiario parecido = new Beneficiario();
        parecido.setId(4L);
        parecido.setNome("João da Silva Santos"); // só o acento muda — normaliza e fica idêntico

        when(beneficiarioRepository.findByAtivoTrue()).thenReturn(List.of(parecido));

        List<Aviso> avisos = regra.avaliar(form);

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("NOME_SIMILAR");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("INFO");
    }

    @Test
    void naoGeraAvisoParaNomesBemDiferentes() {
        RegraNomeSimilar regra = new RegraNomeSimilar(beneficiarioRepository);

        BeneficiarioForm form = new BeneficiarioForm();
        form.setNome("Joao da Silva Santos");

        Beneficiario diferente = new Beneficiario();
        diferente.setId(4L);
        diferente.setNome("Ana Paula Ferreira Costa");

        when(beneficiarioRepository.findByAtivoTrue()).thenReturn(List.of(diferente));

        assertThat(regra.avaliar(form)).isEmpty();
    }

    @Test
    void naoConsultaRepositorioSemNome() {
        RegraNomeSimilar regra = new RegraNomeSimilar(beneficiarioRepository);
        assertThat(regra.avaliar(new BeneficiarioForm())).isEmpty();
    }
}
