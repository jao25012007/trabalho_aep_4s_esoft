package br.edu.educaplan.dao;

import br.edu.educaplan.model.*;
import br.edu.educaplan.util.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TarefaDao {
    private final Conexao banco;
    public TarefaDao(Conexao banco) { this.banco = banco; }
    public List<Tarefa> listar(int idAluno, FiltroTarefa filtro) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT t.* FROM tarefa t JOIN materia m USING(id_materia) WHERE m.id_aluno=?");
        List<Object> parametros = new ArrayList<>(); parametros.add(idAluno);
        if (filtro.idMateria() != null) { sql.append(" AND t.id_materia=?"); parametros.add(filtro.idMateria()); }
        if (filtro.prazoAte() != null) { sql.append(" AND t.data_limite<=?"); parametros.add(filtro.prazoAte()); }
        if (filtro.prioridade() != null) { sql.append(" AND t.prioridade=?"); parametros.add(filtro.prioridade().name()); }
        if (filtro.status() != null) { sql.append(" AND t.status=?"); parametros.add(filtro.status().name()); }
        sql.append(" ORDER BY t.data_limite, t.id_tarefa");
        List<Tarefa> tarefas = new ArrayList<>();
        try (var c = banco.abrir(); var p = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) p.setObject(i + 1, parametros.get(i));
            try (var r = p.executeQuery()) { while (r.next()) tarefas.add(mapear(r)); }
        }
        return tarefas;
    }
    public Optional<Tarefa> buscar(int idAluno, int id) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement("SELECT t.* FROM tarefa t JOIN materia m USING(id_materia) WHERE m.id_aluno=? AND t.id_tarefa=?")) {
            p.setInt(1, idAluno); p.setInt(2, id);
            try (var r = p.executeQuery()) { return r.next() ? Optional.of(mapear(r)) : Optional.empty(); }
        }
    }
    public int criar(int idAluno, Tarefa t) throws SQLException {
        // INSERT ... SELECT também verifica a propriedade da matéria na mesma operação.
        String sql = "INSERT INTO tarefa(id_materia,titulo,descricao,data_limite,prioridade,status) "
            + "SELECT id_materia,?,?,?,?,? FROM materia WHERE id_materia=? AND id_aluno=? RETURNING id_tarefa";
        try (var c = banco.abrir(); var p = c.prepareStatement(sql)) {
            p.setString(1,t.getTitulo()); p.setString(2,t.getDescricao()); p.setObject(3,t.getDataLimite());
            p.setString(4,t.getPrioridade().name()); p.setString(5,t.getStatus().name()); p.setInt(6,t.getIdMateria()); p.setInt(7,idAluno);
            try (var r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Matéria não encontrada para este aluno.");
                return r.getInt(1);
            }
        }
    }
    public void atualizar(int idAluno, Tarefa t) throws SQLException {
        String sql = "UPDATE tarefa t SET titulo=?,descricao=?,data_limite=?,prioridade=?,status=? "
            + "FROM materia m WHERE t.id_materia=m.id_materia AND m.id_aluno=? AND t.id_tarefa=? AND t.id_materia=?";
        try (var c = banco.abrir(); var p = c.prepareStatement(sql)) {
            p.setString(1,t.getTitulo()); p.setString(2,t.getDescricao()); p.setObject(3,t.getDataLimite());
            p.setString(4,t.getPrioridade().name()); p.setString(5,t.getStatus().name());
            p.setInt(6,idAluno); p.setInt(7,t.getId()); p.setInt(8,t.getIdMateria());
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Tarefa não encontrada para este aluno.");
        }
    }
    public void excluir(int idAluno, int id) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement("DELETE FROM tarefa t USING materia m WHERE t.id_materia=m.id_materia AND m.id_aluno=? AND t.id_tarefa=?")) {
            p.setInt(1,idAluno); p.setInt(2,id);
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Tarefa não encontrada para este aluno.");
        }
    }
    private Tarefa mapear(ResultSet r) throws SQLException {
        return new Tarefa(r.getInt("id_tarefa"), r.getInt("id_materia"), r.getString("titulo"), r.getString("descricao"),
            r.getObject("data_limite", java.time.LocalDate.class), Prioridade.valueOf(r.getString("prioridade")), StatusTarefa.valueOf(r.getString("status")));
    }
}
