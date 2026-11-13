package br.edu.educaplan.service;

import br.edu.educaplan.dao.*;
import br.edu.educaplan.model.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EstudoService {
    private final MateriaDao materias;
    private final TarefaDao tarefas;
    public EstudoService(MateriaDao materias, TarefaDao tarefas) { this.materias = materias; this.tarefas = tarefas; }
    public int criarMateria(Aluno aluno, String nome, String descricao) throws SQLException {
        return materias.criar(new Materia(0,aluno.getId(),nome,descricao));
    }
    public List<Materia> listarMaterias(Aluno aluno) throws SQLException { return materias.listar(aluno.getId()); }
    public void atualizarMateria(Aluno aluno, int id, String nome, String descricao) throws SQLException {
        materias.atualizar(new Materia(id,aluno.getId(),nome,descricao));
    }
    public void excluirMateria(Aluno aluno, int id) throws SQLException { materias.excluir(aluno.getId(),id); }
    public int criarTarefa(Aluno aluno, int idMateria, String titulo, String descricao, LocalDate data, Prioridade prioridade, StatusTarefa status) throws SQLException {
        return tarefas.criar(aluno.getId(),new Tarefa(0,idMateria,titulo,descricao,data,prioridade,status));
    }
    public List<Tarefa> listarTarefas(Aluno aluno, FiltroTarefa filtro) throws SQLException { return tarefas.listar(aluno.getId(),filtro); }
    public Tarefa buscarTarefa(Aluno aluno, int id) throws SQLException {
        return tarefas.buscar(aluno.getId(),id).orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada para este aluno."));
    }
    public void atualizarTarefa(Aluno aluno, int id, String titulo, String descricao, LocalDate data, Prioridade prioridade, StatusTarefa status) throws SQLException {
        Tarefa atual = buscarTarefa(aluno,id);
        tarefas.atualizar(aluno.getId(),new Tarefa(id,atual.getIdMateria(),titulo,descricao,data,prioridade,status));
    }
    public void alterarStatus(Aluno aluno, int id, StatusTarefa status) throws SQLException {
        Tarefa tarefa = buscarTarefa(aluno,id); tarefa.alterarStatus(status); tarefas.atualizar(aluno.getId(),tarefa);
    }
    public void excluirTarefa(Aluno aluno, int id) throws SQLException { tarefas.excluir(aluno.getId(),id); }
    public Relatorio relatorio(Aluno aluno) throws SQLException {
        List<Materia> lista = materias.listar(aluno.getId());
        List<Tarefa> atividades = tarefas.listar(aluno.getId(),FiltroTarefa.todos());
        Map<Integer,Materia> porId = lista.stream().collect(Collectors.toMap(Materia::getId,Function.identity()));
        // A composição carregada do banco é usada no cálculo do progresso.
        for (Tarefa t : atividades) {
            Materia m = porId.get(t.getIdMateria());
            if (m != null) m.adicionarTarefa(t);
        }
        return new Relatorio(lista,atividades.size(),contar(atividades,StatusTarefa.PENDENTE),contar(atividades,StatusTarefa.EM_ANDAMENTO),
            contar(atividades,StatusTarefa.CONCLUIDA),atividades.stream().filter(Tarefa::estaAtrasada).count());
    }
    private long contar(List<Tarefa> lista, StatusTarefa status) { return lista.stream().filter(t -> t.getStatus()==status).count(); }
}
