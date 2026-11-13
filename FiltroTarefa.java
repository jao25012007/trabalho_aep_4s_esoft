package br.edu.educaplan.model;
import java.time.LocalDate;
public record FiltroTarefa(Integer idMateria, LocalDate prazoAte, Prioridade prioridade, StatusTarefa status) {
    public FiltroTarefa {
        if (idMateria != null && idMateria <= 0) throw new IllegalArgumentException("ID de matéria inválido.");
    }
    public static FiltroTarefa todos() { return new FiltroTarefa(null, null, null, null); }
}
