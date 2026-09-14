package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.RetiradaForm;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegraRetiranteNaoConfereTest {

    private final RegraRetiranteNaoConfere regra = new RegraRetiranteNaoConfere();

    @Test
    void geraAvisoQuandoPropriaERetiranteNaoBateComOBeneficiario() {
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(1L);
        beneficiario.setNome("Maria da Silva");

        RetiradaForm form = new RetiradaForm();
        form.setTipoRetirante("PROPRIO");
        form.setRetiranteNome("Outra Pessoa");

        var avisos = regra.avaliar(new ContextoAvaliacaoRetirada(form, beneficiario));

        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).getCodigo()).isEqualTo("RETIRANTE_NAO_CONFERE");
    }

    @Test
    void naoGeraAvisoQuandoNomeConfereIgnorandoAcentoECaixa() {
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(1L);
        beneficiario.setNome("João da Silva");

        RetiradaForm form = new RetiradaForm();
        form.setTipoRetirante("PROPRIO");
        form.setRetiranteNome("joao da silva");

        assertThat(regra.avaliar(new ContextoAvaliacaoRetirada(form, beneficiario))).isEmpty();
    }

    @Test
    void naoSeAplicaAPadrinho() {
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(1L);
        beneficiario.setNome("Maria da Silva");

        RetiradaForm form = new RetiradaForm();
        form.setTipoRetirante("PADRINHO");
        form.setRetiranteNome("Fulano Padrinho");

        assertThat(regra.avaliar(new ContextoAvaliacaoRetirada(form, beneficiario))).isEmpty();
    }
}
