package br.edu.educaplan.model;

import br.edu.educaplan.util.Validacao;
import java.util.ArrayList;
import java.util.List;

public final class Aluno extends Usuario {
    private final String matricula;
    private final String curso;
    private final List<Materia> materias = new ArrayList<>();
    public Aluno(int id, String nome, String email, String senhaHash, String matricula, String curso) {
        super(id, nome, email, senhaHash);
        this.matricula = Validacao.texto(matricula, "Matrícula", 30);
        this.curso = Validacao.texto(curso, "Curso", 100);
    }
    public String getMatricula() { return matricula; }
    public String getCurso() { return curso; }
    public List<Materia> getMaterias() { return List.copyOf(materias); }
    public void adicionarMateria(Materia materia) {
        if (materia.getIdAluno() != getId()) throw new IllegalArgumentException("Matéria de outro aluno.");
        materias.add(materia);
    }
    @Override public String exibirResumo() { return "Aluno: " + getNome() + " | Matrícula: " + matricula + " | Curso: " + curso; }
}
