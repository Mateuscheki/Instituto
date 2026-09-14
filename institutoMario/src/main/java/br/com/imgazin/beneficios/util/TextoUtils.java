package br.com.imgazin.beneficios.util;

import java.text.Normalizer;

/**
 * Comparação aproximada de texto para o aviso {@code NOME_SIMILAR}. O
 * projeto usa MySQL (sem a extensão {@code pg_trgm}/{@code similarity} do
 * Postgres), então a similaridade é calculada em Java: normaliza (sem
 * acento, minúsculo) e mede a distância de Levenshtein, convertida em uma
 * razão de 0 a 1 (1 = idêntico).
 */
public final class TextoUtils {

    private TextoUtils() {
    }

    public static String normalizarSemAcento(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    /** 1.0 = textos idênticos (após normalizar); 0.0 = completamente diferentes. */
    public static double similaridade(String a, String b) {
        String x = normalizarSemAcento(a);
        String y = normalizarSemAcento(b);

        if (x.isEmpty() && y.isEmpty()) {
            return 1.0;
        }
        int maiorTamanho = Math.max(x.length(), y.length());
        if (maiorTamanho == 0) {
            return 1.0;
        }
        int distancia = distanciaLevenshtein(x, y);
        return 1.0 - ((double) distancia / maiorTamanho);
    }

    private static int distanciaLevenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int custoSubstituicao = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + custoSubstituicao
                );
            }
        }
        return dp[a.length()][b.length()];
    }
}
