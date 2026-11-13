package br.edu.educaplan.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexao {
    private final String url;
    private final Properties propriedades = new Properties();
    public Conexao(Configuracao configuracao) {
        url = configuracao.obrigatoria("DB_URL");
        if (!url.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException("DB_URL deve começar com jdbc:postgresql://");
        propriedades.setProperty("user", configuracao.obrigatoria("DB_USER"));
        propriedades.setProperty("password", configuracao.obrigatoria("DB_PASSWORD"));
        propriedades.setProperty("currentSchema", "educaplan");
        propriedades.setProperty("connectTimeout", "10");
        propriedades.setProperty("socketTimeout", "30");
        propriedades.setProperty("ApplicationName", "EducaPlan");
    }
    public Connection abrir() throws SQLException { return DriverManager.getConnection(url, propriedades); }
    public void verificar() throws SQLException {
        try (var conexao = abrir(); var comando = conexao.prepareStatement("SELECT COUNT(*) FROM educaplan.usuario");
             var resultado = comando.executeQuery()) { resultado.next(); }
    }
}
