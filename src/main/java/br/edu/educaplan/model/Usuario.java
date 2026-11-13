package br.edu.educaplan.model;

import br.edu.educaplan.util.SenhaUtil;
import br.edu.educaplan.util.Validacao;

public abstract class Usuario {
    private final int id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    protected Usuario(int id, String nome, String email, String senhaHash) {
        this.id = id;
        this.nome = Validacao.texto(nome, "Nome", 120);
        this.email = Validacao.email(email);
        this.senhaHash = senhaHash;
    }
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public boolean autenticar(char[] senha) { return SenhaUtil.verificar(senha, senhaHash); }
    public abstract String exibirResumo();
}
