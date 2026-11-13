package br.edu.educaplan.model;

import br.edu.educaplan.util.Validacao;
import java.time.LocalDate;
import java.util.Objects;

public final class Tarefa {
    private final int id;
    private final int idMateria;
    private final String titulo;
    private final String descricao;
    private final LocalDate dataLimite;
    private final Prioridade prioridade;
    private StatusTarefa status;
    public Tarefa(int id, int idMateria, String titulo, String descricao, LocalDate dataLimite, Prioridade prioridade, StatusTarefa status) {
        if (idMateria <= 0) throw new IllegalArgumentException("Selecione uma matéria válida.");
        this.id = id;
        this.idMateria = idMateria;
        this.titulo = Validacao.texto(titulo, "Título", 120);
        this.descricao = Validacao.opcional(descricao, 255);
        this.dataLimite = Objects.requireNonNull(dataLimite, "Prazo obrigatório.");
        this.prioridade = Objects.requireNonNull(prioridade, "Prioridade obrigatória.");
        alterarStatus(status);
    }
    public int getId() { return id; }
    public int getIdMateria() { return idMateria; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDate getDataLimite() { return dataLimite; }
    public Prioridade getPrioridade() { return prioridade; }
    public StatusTarefa getStatus() { return status; }
    public void alterarStatus(StatusTarefa status) { this.status = Objects.requireNonNull(status, "Status obrigatório."); }
    public boolean estaAtrasada() { return status != StatusTarefa.CONCLUIDA && dataLimite.isBefore(LocalDate.now()); }
}
