package br.edu.educaplan.dao;

import br.edu.educaplan.model.*;
import br.edu.educaplan.util.Conexao;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class MateriaDao {
    private final Conexao banco;
    public MateriaDao(Conexao banco) { this.banco = banco; }
    public int criar(Materia m) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement("INSERT INTO materia(id_aluno,nome,descricao) VALUES(?,?,?) RETURNING id_materia")) {
            p.setInt(1,m.getIdAluno()); p.setString(2,m.getNome()); p.setString(3,m.getDescricao());
            try (var r = p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }
    public List<Materia> listar(int idAluno) throws SQLException {
        List<Materia> materias = new ArrayList<>();
        try (var c = banco.abrir(); var p = c.prepareStatement("SELECT * FROM materia WHERE id_aluno=? ORDER BY nome,id_materia")) {
            p.setInt(1,idAluno);
            try (var r = p.executeQuery()) {
                while (r.next()) materias.add(new Materia(r.getInt("id_materia"), idAluno, r.getString("nome"), r.getString("descricao")));
            }
        }
        return materias;
    }
    public void atualizar(Materia m) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement("UPDATE materia SET nome=?,descricao=? WHERE id_materia=? AND id_aluno=?")) {
            p.setString(1,m.getNome()); p.setString(2,m.getDescricao()); p.setInt(3,m.getId()); p.setInt(4,m.getIdAluno());
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Matéria não encontrada para este aluno.");
        }
    }
    public void excluir(int idAluno, int id) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement("DELETE FROM materia WHERE id_materia=? AND id_aluno=?")) {
            p.setInt(1,id); p.setInt(2,idAluno);
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Matéria não encontrada para este aluno.");
        }
    }
}
