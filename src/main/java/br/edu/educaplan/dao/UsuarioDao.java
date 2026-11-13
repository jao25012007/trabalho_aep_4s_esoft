package br.edu.educaplan.dao;

import br.edu.educaplan.model.*;
import br.edu.educaplan.util.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UsuarioDao {
    private final Conexao banco;
    private static final String CONSULTA = "SELECT u.*, a.matricula, a.curso, ad.cargo FROM usuario u "
        + "LEFT JOIN aluno a USING(id_usuario) LEFT JOIN administrador ad USING(id_usuario) ";
    public UsuarioDao(Conexao banco) { this.banco = banco; }
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement(CONSULTA + "WHERE u.email=?")) {
            p.setString(1, email);
            try (var r = p.executeQuery()) { return r.next() ? Optional.of(mapear(r)) : Optional.empty(); }
        }
    }
    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement(CONSULTA + "WHERE u.id_usuario=?")) {
            p.setInt(1, id);
            try (var r = p.executeQuery()) { return r.next() ? Optional.of(mapear(r)) : Optional.empty(); }
        }
    }
    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (var c = banco.abrir(); var p = c.prepareStatement(CONSULTA + "ORDER BY u.id_usuario"); var r = p.executeQuery()) {
            while (r.next()) usuarios.add(mapear(r));
        }
        return usuarios;
    }
    public Usuario criar(Usuario usuario, boolean somentePrimeiroAdmin) throws SQLException {
        try (var c = banco.abrir()) {
            c.setAutoCommit(false);
            try {
                if (somentePrimeiroAdmin) {
                    // Serializa duas tentativas concorrentes de inicialização.
                    try (var p = c.prepareStatement("SELECT pg_advisory_xact_lock(2026, 401)")) { p.execute(); }
                    try (var p = c.prepareStatement("SELECT 1 FROM administrador"); var r = p.executeQuery()) {
                        if (r.next()) throw new IllegalArgumentException("Já existe administrador. Entre com ele para cadastrar outro.");
                    }
                }
                String tipo = usuario instanceof Aluno ? "ALUNO" : "ADMINISTRADOR";
                int id;
                try (var p = c.prepareStatement("INSERT INTO usuario(nome,email,senha_hash,tipo) VALUES(?,?,?,?) RETURNING id_usuario")) {
                    p.setString(1, usuario.getNome()); p.setString(2, usuario.getEmail());
                    p.setString(3, usuario.getSenhaHash()); p.setString(4, tipo);
                    try (var r = p.executeQuery()) { r.next(); id = r.getInt(1); }
                }
                if (usuario instanceof Aluno aluno) {
                    try (var p = c.prepareStatement("INSERT INTO aluno(id_usuario,matricula,curso) VALUES(?,?,?)")) {
                        p.setInt(1, id); p.setString(2, aluno.getMatricula()); p.setString(3, aluno.getCurso()); p.executeUpdate();
                    }
                } else {
                    try (var p = c.prepareStatement("INSERT INTO administrador(id_usuario,cargo) VALUES(?,?)")) {
                        p.setInt(1, id); p.setString(2, ((Administrador) usuario).getCargo()); p.executeUpdate();
                    }
                }
                c.commit();
                if (usuario instanceof Aluno a) return new Aluno(id, a.getNome(), a.getEmail(), a.getSenhaHash(), a.getMatricula(), a.getCurso());
                Administrador a = (Administrador) usuario;
                return new Administrador(id, a.getNome(), a.getEmail(), a.getSenhaHash(), a.getCargo());
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public void atualizar(Usuario usuario) throws SQLException {
        try (var c = banco.abrir()) {
            c.setAutoCommit(false);
            try {
                try (var p = c.prepareStatement("UPDATE usuario SET nome=?,email=? WHERE id_usuario=?")) {
                    p.setString(1, usuario.getNome()); p.setString(2, usuario.getEmail()); p.setInt(3, usuario.getId());
                    if (p.executeUpdate() != 1) throw new IllegalArgumentException("Usuário não encontrado.");
                }
                if (usuario instanceof Aluno a) {
                    try (var p = c.prepareStatement("UPDATE aluno SET matricula=?,curso=? WHERE id_usuario=?")) {
                        p.setString(1, a.getMatricula()); p.setString(2, a.getCurso()); p.setInt(3, a.getId()); p.executeUpdate();
                    }
                } else {
                    try (var p = c.prepareStatement("UPDATE administrador SET cargo=? WHERE id_usuario=?")) {
                        p.setString(1, ((Administrador) usuario).getCargo()); p.setInt(2, usuario.getId()); p.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public boolean excluir(int id) throws SQLException {
        try (var c = banco.abrir(); var p = c.prepareStatement("DELETE FROM usuario WHERE id_usuario=?")) {
            p.setInt(1, id); return p.executeUpdate() == 1;
        }
    }
    private Usuario mapear(ResultSet r) throws SQLException {
        int id = r.getInt("id_usuario"); String nome = r.getString("nome"), email = r.getString("email"), hash = r.getString("senha_hash");
        if (r.getString("tipo").equals("ALUNO")) return new Aluno(id, nome, email, hash, r.getString("matricula"), r.getString("curso"));
        return new Administrador(id, nome, email, hash, r.getString("cargo"));
    }
}
