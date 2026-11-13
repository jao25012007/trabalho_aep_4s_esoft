package br.edu.educaplan.model;

import br.edu.educaplan.util.Validacao;
import java.util.ArrayList;
import java.util.List;

public final class Materia {
    private final int id;
    private final int idAluno;
    private final String nome;
    private final String descricao;
    private final List<Tarefa> tarefas = new ArrayList<>();
    public Materia(int id, int idAluno, String nome, String descricao) {
        if (idAluno <= 0) throw new IllegalArgumentException("Aluno inválido.");
        this.id = id;
        this.idAluno = idAluno;
        this.nome = Validacao.texto(nome, "Nome da matéria", 100);
        this.descricao = Validacao.opcional(descricao, 255);
    }
    public int getId() { return id; }
    public int getIdAluno() { return idAluno; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public List<Tarefa> getTarefas() { return List.copyOf(tarefas); }
    public void adicionarTarefa(Tarefa tarefa) {
        if (tarefa.getIdMateria() != id) throw new IllegalArgumentException("Tarefa de outra matéria.");
        tarefas.add(tarefa);
    }
    public double calcularProgresso() {
        if (tarefas.isEmpty()) return 0.0;
        long concluidas = tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.CONCLUIDA).count();
        return 100.0 * concluidas / tarefas.size();
    }
}
