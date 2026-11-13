package br.edu.educaplan.model;
import java.util.List;
public record Relatorio(List<Materia> materias, long total, long pendentes, long emAndamento, long concluidas, long atrasadas) {
    public Relatorio { materias = List.copyOf(materias); }
    public double percentualConclusao() { return total == 0 ? 0.0 : 100.0 * concluidas / total; }
}
