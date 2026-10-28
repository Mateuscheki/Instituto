package br.com.imgazin.beneficios.util;

import java.text.Normalizer;

public final class EnderecoUtils {

    private EnderecoUtils() {
    }

    public static String normalizarCep(String cep) {
        if (cep == null) {
            return null;
        }
        return cep.replaceAll("\\D", "");
    }

    public static String calcularChaveResidencia(String cep, String numero, String complemento) {
        return normalizarTexto(normalizarCep(cep)) + normalizarTexto(numero) + normalizarTexto(complemento);
    }

    private static String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().replaceAll("\\s+", "");
    }
}
