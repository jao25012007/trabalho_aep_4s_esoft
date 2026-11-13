package br.edu.educaplan.model;

import br.edu.educaplan.util.Validacao;

public final class Administrador extends Usuario {
    private final String cargo;
    public Administrador(int id, String nome, String email, String senhaHash, String cargo) {
        super(id, nome, email, senhaHash);
        this.cargo = Validacao.texto(cargo, "Cargo", 80);
    }
    public String getCargo() { return cargo; }
    @Override public String exibirResumo() { return "Administrador: " + getNome() + " | Cargo: " + cargo; }
}
