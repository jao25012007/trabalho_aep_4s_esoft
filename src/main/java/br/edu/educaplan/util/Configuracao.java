package br.edu.educaplan.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Variáveis de ambiente têm precedência sobre o arquivo .env local. */
public final class Configuracao {
    private final Map<String, String> valores = new HashMap<>();
    public Configuracao() throws IOException {
        Path arquivo = Path.of(".env");
        if (Files.isRegularFile(arquivo)) {
            for (String linha : Files.readAllLines(arquivo)) {
                linha = linha.strip();
                if (linha.isEmpty() || linha.startsWith("#")) continue;
                int separador = linha.indexOf('=');
                if (separador < 1) throw new IllegalArgumentException("Linha inválida no arquivo .env.");
                String valor = linha.substring(separador + 1).strip();
                if (valor.length() >= 2 && ((valor.startsWith("\"") && valor.endsWith("\""))
                        || (valor.startsWith("'") && valor.endsWith("'")))) valor = valor.substring(1, valor.length() - 1);
                valores.put(linha.substring(0, separador).strip(), valor);
            }
        }
    }
    public String obrigatoria(String chave) {
        String valor = System.getenv(chave);
        if (valor == null) valor = valores.get(chave);
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Configure " + chave + " no .env (veja .env.example).");
        return valor;
    }
}
