package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegraJaRetirouNoMesTest {

    @Mock
    private RetiradaCestaRepository retiradaCestaRepository;

    @Test
    void geraAvisoAltaQuandoJaExisteRetiradaValidaNoMes() {
        RegraJaRetirouNoMes regra = new RegraJaRetirouNoMes(retiradaCestaRepository);

        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(1L);

        RetiradaForm form = new RetiradaForm();
        form.setMesReferencia("2026-09");

        RetiradaCesta existente = new RetiradaCesta();
        existente.setId(50L);

        when(retiradaCestaRepository.findByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(1L, LocalDate.of(2026, 9, 1)))
                .thenReturn(Optional.of(existente));

        var avisos = regra.avaliar(new ContextoAvaliacaoRetirada(form, beneficiario));

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("JA_RETIROU_NO_MES");
        assertThat(avisos.get(0).getSeveridade().name()).isEqualTo("ALTA");
        assertThat(avisos.get(0).getReferenciaId()).isEqualTo(50L);
    }

    @Test
    void naoGeraAvisoQuandoNaoHaRetiradaNoMes() {
        RegraJaRetirouNoMes regra = new RegraJaRetirouNoMes(retiradaCestaRepository);

        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(1L);
        RetiradaForm form = new RetiradaForm();
        form.setMesReferencia("2026-09");

        when(retiradaCestaRepository.findByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(1L, LocalDate.of(2026, 9, 1)))
                .thenReturn(Optional.empty());

        assertThat(regra.avaliar(new ContextoAvaliacaoRetirada(form, beneficiario))).isEmpty();
    }
}
