package br.com.imgazin.beneficios.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CPF de teste usado: 111.444.777-35 — dígitos verificadores conferidos à
 * mão pelo algoritmo da Receita Federal (documentado no javadoc de
 * {@link CpfUtils#isValido(String)}), não inventado.
 */
class CpfUtilsTest {

    private static final String CPF_VALIDO_FORMATADO = "111.444.777-35";
    private static final String CPF_VALIDO_NORMALIZADO = "11144477735";

    @Test
    void normalizarRemoveTudoQueNaoEhDigito() {
        assertThat(CpfUtils.normalizar(CPF_VALIDO_FORMATADO)).isEqualTo(CPF_VALIDO_NORMALIZADO);
    }

    @Test
    void normalizarAceitaNulo() {
        assertThat(CpfUtils.normalizar(null)).isNull();
    }

    @Test
    void formatarAdicionaPontuacaoEHifen() {
        assertThat(CpfUtils.formatar(CPF_VALIDO_NORMALIZADO)).isEqualTo(CPF_VALIDO_FORMATADO);
    }

    @Test
    void formatarDevolveValorOriginalSeNaoTiverOnzeDigitos() {
        assertThat(CpfUtils.formatar("123")).isEqualTo("123");
    }

    @Test
    void mascararEscondePrimeiroGrupoEDigitosVerificadores() {
        assertThat(CpfUtils.mascarar(CPF_VALIDO_NORMALIZADO)).isEqualTo("***.444.777-**");
    }

    @Test
    void mascararDevolveTudoMascaradoSeNaoTiverOnzeDigitos() {
        assertThat(CpfUtils.mascarar("123")).isEqualTo("***.***.***-**");
        assertThat(CpfUtils.mascarar(null)).isEqualTo("***.***.***-**");
    }

    @Test
    void isValidoAceitaCpfComDigitoVerificadorCorreto() {
        assertThat(CpfUtils.isValido(CPF_VALIDO_FORMATADO)).isTrue();
        assertThat(CpfUtils.isValido(CPF_VALIDO_NORMALIZADO)).isTrue();
    }

    @Test
    void isValidoRejeitaDigitoVerificadorErrado() {
        assertThat(CpfUtils.isValido("11144477736")).isFalse();
    }

    @Test
    void isValidoRejeitaTodosOsDigitosIguais() {
        assertThat(CpfUtils.isValido("00000000000")).isFalse();
        assertThat(CpfUtils.isValido("11111111111")).isFalse();
    }

    @Test
    void isValidoRejeitaTamanhoErradoOuNulo() {
        assertThat(CpfUtils.isValido("123")).isFalse();
        assertThat(CpfUtils.isValido("")).isFalse();
        assertThat(CpfUtils.isValido(null)).isFalse();
    }
}
