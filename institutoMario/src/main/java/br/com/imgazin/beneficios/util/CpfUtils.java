package br.com.imgazin.beneficios.util;

public final class CpfUtils {

    private CpfUtils() {
    }

    public static String normalizar(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }

    public static String formatar(String cpf) {
        String normalizado = normalizar(cpf);
        if (normalizado == null || normalizado.length() != 11) {
            return cpf;
        }
        return normalizado.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    public static String mascarar(String cpf) {
        String normalizado = normalizar(cpf);
        if (normalizado == null || normalizado.length() != 11) {
            return "***.***.***-**";
        }
        return "***." + normalizado.substring(3, 6) + "." + normalizado.substring(6, 9) + "-**";
    }

    /**
     * Valida os dois dígitos verificadores pelo algoritmo oficial da Receita
     * Federal. CPFs com todos os dígitos iguais (ex.: 00000000000,
     * 11111111111) são explicitamente inválidos, mesmo que "passem" na conta.
     */
    public static boolean isValido(String cpf) {
        String normalizado = normalizar(cpf);
        if (normalizado == null || normalizado.length() != 11) {
            return false;
        }
        if (normalizado.chars().distinct().count() == 1) {
            return false;
        }

        int[] digitos = normalizado.chars().map(c -> c - '0').toArray();

        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            soma1 += digitos[i] * (10 - i);
        }
        int primeiroDigitoVerificador = calcularDigitoVerificador(soma1);
        if (primeiroDigitoVerificador != digitos[9]) {
            return false;
        }

        int soma2 = 0;
        for (int i = 0; i < 10; i++) {
            soma2 += digitos[i] * (11 - i);
        }
        int segundoDigitoVerificador = calcularDigitoVerificador(soma2);
        return segundoDigitoVerificador == digitos[10];
    }

    private static int calcularDigitoVerificador(int somaPonderada) {
        int resto = (somaPonderada * 10) % 11;
        return (resto == 10) ? 0 : resto;
    }
}
