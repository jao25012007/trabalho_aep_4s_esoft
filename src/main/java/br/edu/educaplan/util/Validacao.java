package br.edu.educaplan.util;

import java.util.Locale;

public final class Validacao {
    private Validacao() {}
    public static String texto(String valor, String campo, int limite) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " é obrigatório.");
        caracteresValidos(valor, campo);
        String texto = valor.strip();
        if (texto.length() > limite) throw new IllegalArgumentException(campo + " aceita até " + limite + " caracteres.");
        return texto;
    }
    public static String opcional(String valor, int limite) {
        if (valor != null) caracteresValidos(valor, "Descrição");
        String texto = valor == null ? "" : valor.strip();
        if (texto.length() > limite) throw new IllegalArgumentException("Descrição aceita até " + limite + " caracteres.");
        return texto;
    }
    public static String email(String valor) {
        String email = texto(valor, "E-mail", 160).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("E-mail inválido.");
        return email;
    }
    public static void senha(char[] senha) {
        if (senha == null || senha.length < 8 || senha.length > 128)
            throw new IllegalArgumentException("A senha deve ter de 8 a 128 caracteres.");
        for (char caractere : senha) {
            if (caractere == '\0') throw new IllegalArgumentException("A senha contém um caractere nulo recebido do terminal. Digite novamente.");
        }
    }
    /** PostgreSQL UTF-8 aceita acentos e emojis, mas não U+0000 ou Unicode incompleto. */
    private static void caracteresValidos(String valor, String campo) {
        for (int i = 0; i < valor.length(); i++) {
            char atual = valor.charAt(i);
            if (atual == '\0') throw new IllegalArgumentException(campo + " contém um caractere nulo recebido do terminal. Digite novamente.");
            if (Character.isHighSurrogate(atual)) {
                if (i + 1 < valor.length() && Character.isLowSurrogate(valor.charAt(i + 1))) { i++; continue; }
                throw new IllegalArgumentException(campo + " contém um caractere Unicode incompleto. Digite novamente.");
            }
            if (Character.isLowSurrogate(atual)) throw new IllegalArgumentException(campo + " contém um caractere Unicode incompleto. Digite novamente.");
        }
    }
}
