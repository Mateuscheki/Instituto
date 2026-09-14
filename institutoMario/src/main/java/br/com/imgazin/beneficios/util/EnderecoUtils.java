package br.com.imgazin.beneficios.util;

import java.text.Normalizer;

/**
 * Utilitários de endereço do módulo Benefícios.
 */
public final class EnderecoUtils {

    private EnderecoUtils() {
    }

    /** Remove tudo que não for dígito (CEP digitado com ou sem hífen). */
    public static String normalizarCep(String cep) {
        if (cep == null) {
            return null;
        }
        return cep.replaceAll("\\D", "");
    }

    /**
     * Chave normalizada usada para detectar famílias no mesmo endereço:
     * cep + número + complemento, em minúsculo, sem acento e sem espaço —
     * exatamente como descrito no modelo de dados do módulo. Concatenação
     * direta (sem separador) por especificação; ciente de que, em tese, dois
     * conjuntos diferentes de cep/número/complemento poderiam colidir na
     * concatenação — na prática o CEP tem tamanho fixo (8 dígitos), o que já
     * evita a colisão mais óbvia entre cep e número.
     */
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
