package br.edu.educaplan.service;

import br.edu.educaplan.dao.UsuarioDao;
import br.edu.educaplan.model.*;
import br.edu.educaplan.util.SenhaUtil;
import br.edu.educaplan.util.Validacao;
import java.sql.SQLException;
import java.util.List;

public final class UsuarioService {
    private final UsuarioDao dao;
    public UsuarioService(UsuarioDao dao) { this.dao = dao; }
    public Aluno cadastrarAluno(String nome, String email, char[] senha, String matricula, String curso) throws SQLException {
        return (Aluno) dao.criar(new Aluno(0, nome, email, SenhaUtil.gerarHash(senha), matricula, curso), false);
    }
    public Administrador criarAdministradorInicial(String nome, String email, char[] senha, String cargo) throws SQLException {
        return (Administrador) dao.criar(new Administrador(0, nome, email, SenhaUtil.gerarHash(senha), cargo), true);
    }
    public Usuario autenticar(String email, char[] senha) throws SQLException {
        Usuario usuario = dao.buscarPorEmail(Validacao.email(email)).orElse(null);
        if (usuario == null || !usuario.autenticar(senha)) throw new IllegalArgumentException("E-mail ou senha incorretos.");
        return usuario;
    }
    public List<Usuario> listar(Usuario ator) throws SQLException { exigirAdmin(ator); return dao.listar(); }
    public Usuario buscar(Usuario ator, int id) throws SQLException {
        exigirAdmin(ator); return dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
    public Administrador cadastrarAdministrador(Usuario ator, String nome, String email, char[] senha, String cargo) throws SQLException {
        exigirAdmin(ator);
        return (Administrador) dao.criar(new Administrador(0,nome,email,SenhaUtil.gerarHash(senha),cargo),false);
    }
    public void atualizar(Usuario ator, Usuario usuario) throws SQLException {
        exigirAdmin(ator);
        Usuario existente = buscar(ator, usuario.getId());
        if (!existente.getClass().equals(usuario.getClass())) throw new IllegalArgumentException("O perfil do usuário não pode ser alterado.");
        dao.atualizar(usuario);
    }
    public void excluir(Usuario ator, int id) throws SQLException {
        exigirAdmin(ator);
        if (ator.getId() == id) throw new IllegalArgumentException("O administrador não pode excluir sua própria conta.");
        if (!dao.excluir(id)) throw new IllegalArgumentException("Usuário não encontrado.");
    }
    private void exigirAdmin(Usuario usuario) {
        if (!(usuario instanceof Administrador)) throw new IllegalArgumentException("Acesso exclusivo de administradores.");
    }
}
