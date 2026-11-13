package br.edu.educaplan;

import br.edu.educaplan.dao.*;
import br.edu.educaplan.model.*;
import br.edu.educaplan.service.*;
import br.edu.educaplan.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Testes executáveis sem JUnit, exclusivamente em PostgreSQL de teste. */
public final class TesteIntegracao {
    private static int verificacoes;
    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) throw new AssertionError(descricao);
        verificacoes++; System.out.println("OK: " + descricao);
    }
    private static void rejeitar(Acao acao, String descricao) throws Exception {
        try { acao.executar(); }
        catch (IllegalArgumentException | SQLException e) { verificar(true,descricao); return; }
        throw new AssertionError("Operação indevidamente aceita: " + descricao);
    }
    private static void rejeitarNaValidacao(Acao acao, String descricao) throws Exception {
        try { acao.executar(); }
        catch (IllegalArgumentException e) { verificar(true,descricao); return; }
        throw new AssertionError("Texto inválido não foi bloqueado antes do SQL: " + descricao);
    }
    @FunctionalInterface private interface Acao { void executar() throws Exception; }
    public static void main(String[] args) throws Exception {
        Configuracao config = new Configuracao();
        if (!"SIM".equals(System.getenv("EDUCAPLAN_TEST_DB"))
                || !config.obrigatoria("DB_URL").matches("jdbc:postgresql://.+/educaplan_teste(?:\\?.*)?$"))
            throw new IllegalArgumentException("Use EDUCAPLAN_TEST_DB=SIM e um banco separado chamado educaplan_teste.");
        Conexao banco = new Conexao(config);
        try (var c = banco.abrir(); var p = c.createStatement()) { p.execute(Files.readString(Path.of("database/educaplan.sql"))); }
        UsuarioDao ud = new UsuarioDao(banco); MateriaDao md = new MateriaDao(banco); TarefaDao td = new TarefaDao(banco);
        UsuarioService us = new UsuarioService(ud); EstudoService es = new EstudoService(md,td);
        List<Integer> criados = new ArrayList<>(); String chave = "teste" + System.nanoTime();
        char[] senha = "SenhaTeste123!".toCharArray();
        try {
            boolean jaTemAdmin = ud.listar().stream().anyMatch(u -> u instanceof Administrador);
            Administrador admin = jaTemAdmin
                ? (Administrador)ud.criar(new Administrador(0,"Admin teste",chave+"admin@exemplo.com",SenhaUtil.gerarHash(senha),"Teste"),false)
                : us.criarAdministradorInicial("Admin teste",chave+"admin@exemplo.com",senha,"Teste");
            criados.add(admin.getId());
            rejeitar(() -> us.criarAdministradorInicial("Intruso",chave+"intruso@exemplo.com",senha,"Teste"),"bloqueio de segundo administrador inicial");
            Aluno a = us.cadastrarAluno("João — Ação e Educação",chave+"a@exemplo.com",senha,"RA1","Engenharia de Software"); criados.add(a.getId());
            Aluno b = us.cadastrarAluno("Aluno B",chave+"b@exemplo.com",senha,"RA2","Engenharia de Software"); criados.add(b.getId());
            verificar(us.autenticar(a.getEmail().toUpperCase(),senha) instanceof Aluno,"cadastro e login persistidos com e-mail normalizado");
            verificar(ud.buscarPorId(a.getId()).orElseThrow().getNome().equals("João — Ação e Educação"),"nome com acentos preservado no PostgreSQL");
            rejeitarNaValidacao(() -> new Aluno(0,"Jo\0ão",a.getEmail(),a.getSenhaHash(),"RA","Curso"),"caractere nulo no nome bloqueado antes do SQL");
            rejeitarNaValidacao(() -> es.criarMateria(a,"Matéria","Descrição\0"),"caractere nulo na descrição bloqueado antes do SQL");
            rejeitarNaValidacao(() -> es.criarMateria(a,"Matéria \uD800",""),"Unicode incompleto bloqueado antes do SQL");
            verificar(Validacao.texto("Educação \uD83D\uDCDA","Nome",100).equals("Educação \uD83D\uDCDA"),"caracteres Unicode suplementares válidos preservados");
            rejeitar(() -> us.autenticar(a.getEmail(),"errada".toCharArray()),"senha incorreta rejeitada");
            rejeitar(() -> us.autenticar(chave+"ausente@exemplo.com",senha),"usuário inexistente rejeitado");
            verificar(!a.getSenhaHash().contains("SenhaTeste123!"),"senha não armazenada em texto puro");
            verificar(!SenhaUtil.gerarHash(senha).equals(a.getSenhaHash()),"sal diferente para a mesma senha");
            rejeitar(() -> us.cadastrarAluno("Duplicado",a.getEmail(),senha,"RA3","Curso"),"e-mail duplicado rejeitado");
            rejeitar(() -> us.cadastrarAluno("Fraca",chave+"fraca@exemplo.com","123".toCharArray(),"RA","Curso"),"senha curta rejeitada");
            rejeitar(() -> us.listar(a),"aluno não pode administrar usuários");
            Usuario polimorfico = a;
            verificar(polimorfico.exibirResumo().startsWith("Aluno:"),"polimorfismo no resumo do aluno");
            polimorfico = admin;
            verificar(polimorfico.exibirResumo().startsWith("Administrador:"),"polimorfismo no resumo do administrador");
            int m1 = es.criarMateria(a,"POO ' OR 1=1 --","Descrição com acentos");
            int m2 = es.criarMateria(a,"Banco de dados","");
            int mb = es.criarMateria(b,"Matéria privada","");
            verificar(es.listarMaterias(a).size()==2,"CRUD: criação e leitura de matérias");
            es.atualizarMateria(a,m1,"POO atualizada","Java");
            verificar(es.listarMaterias(a).stream().anyMatch(m -> m.getNome().equals("POO atualizada")),"CRUD: atualização de matéria");
            rejeitar(() -> es.atualizarMateria(b,m1,"Invadida",""),"bloqueio de edição de matéria alheia");
            rejeitar(() -> es.excluirMateria(b,m1),"bloqueio de exclusão de matéria alheia");
            rejeitar(() -> es.criarMateria(a,"  ",""),"matéria sem nome rejeitada");
            LocalDate hoje = LocalDate.now();
            int t1 = es.criarTarefa(a,m1,"Revisar herança","Capítulo 3",hoje.minusDays(1),Prioridade.ALTA,StatusTarefa.PENDENTE);
            int t2 = es.criarTarefa(a,m1,"Praticar interfaces","",hoje.plusDays(3),Prioridade.MEDIA,StatusTarefa.CONCLUIDA);
            int t3 = es.criarTarefa(a,m2,"Modelar banco","",hoje.plusDays(1),Prioridade.BAIXA,StatusTarefa.EM_ANDAMENTO);
            es.criarTarefa(b,mb,"Privada","",hoje,Prioridade.ALTA,StatusTarefa.PENDENTE);
            verificar(es.listarTarefas(a,FiltroTarefa.todos()).size()==3,"CRUD: criação e leitura de tarefas isoladas por aluno");
            verificar(es.buscarTarefa(a,t1).estaAtrasada(),"detecção de tarefa atrasada");
            rejeitar(() -> es.criarTarefa(a,mb,"Inválida","",hoje,Prioridade.ALTA,StatusTarefa.PENDENTE),"bloqueio de criação em matéria alheia");
            rejeitar(() -> es.buscarTarefa(b,t1),"bloqueio de leitura de tarefa alheia");
            rejeitar(() -> es.atualizarTarefa(b,t1,"Invadida","",hoje,Prioridade.ALTA,StatusTarefa.PENDENTE),"bloqueio de edição de tarefa alheia");
            rejeitar(() -> es.excluirTarefa(b,t1),"bloqueio de exclusão de tarefa alheia");
            verificar(es.listarTarefas(a,new FiltroTarefa(m1,null,null,null)).size()==2,"filtro por matéria");
            verificar(es.listarTarefas(a,new FiltroTarefa(null,hoje,null,null)).size()==1,"filtro por prazo máximo");
            verificar(es.listarTarefas(a,new FiltroTarefa(null,null,Prioridade.ALTA,null)).size()==1,"filtro por prioridade");
            verificar(es.listarTarefas(a,new FiltroTarefa(null,null,null,StatusTarefa.CONCLUIDA)).size()==1,"filtro por status");
            verificar(es.listarTarefas(a,new FiltroTarefa(m1,hoje,Prioridade.ALTA,StatusTarefa.PENDENTE)).size()==1,"combinação de quatro filtros");
            Relatorio r = es.relatorio(a);
            verificar(r.total()==3 && r.pendentes()==1 && r.emAndamento()==1 && r.concluidas()==1 && r.atrasadas()==1,"relatório de totais e atrasos");
            verificar(Math.abs(r.percentualConclusao()-100.0/3)<0.001,"percentual geral de conclusão");
            verificar(r.materias().stream().filter(m -> m.getId()==m1).findFirst().orElseThrow().calcularProgresso()==50.0,"composição matéria/tarefas e progresso");
            es.alterarStatus(a,t1,StatusTarefa.CONCLUIDA);
            verificar(!es.buscarTarefa(a,t1).estaAtrasada() && es.relatorio(a).atrasadas()==0,"status persistido recalcula atrasos");
            verificar(es.relatorio(a).materias().stream().filter(m -> m.getId()==m1).findFirst().orElseThrow().calcularProgresso()==100.0,"status persistido recalcula progresso");
            es.atualizarTarefa(a,t3,"DER revisado","Descrição nova",hoje.plusDays(2),Prioridade.ALTA,StatusTarefa.PENDENTE);
            Tarefa alterada = es.buscarTarefa(a,t3);
            verificar(alterada.getTitulo().equals("DER revisado") && alterada.getPrioridade()==Prioridade.ALTA && alterada.getDataLimite().equals(hoje.plusDays(2)),"CRUD: edição completa de tarefa");
            // Uma conexão/DAO novos precisam encontrar os dados já gravados.
            verificar(new TarefaDao(new Conexao(new Configuracao())).buscar(a.getId(),t3).isPresent(),"persistência em nova conexão");
            es.excluirTarefa(a,t2);
            rejeitar(() -> es.buscarTarefa(a,t2),"CRUD: exclusão de tarefa");
            es.excluirMateria(a,m1);
            rejeitar(() -> es.buscarTarefa(a,t1),"exclusão de matéria remove tarefas por composição");
            verificar(es.listarMaterias(a).size()==1,"CRUD: exclusão de matéria");
            Aluno editado = new Aluno(a.getId(),"Nome atualizado",chave+"novo@exemplo.com",a.getSenhaHash(),"RA-NOVO","Curso atualizado");
            us.atualizar(admin,editado);
            verificar(us.autenticar(editado.getEmail(),senha).getNome().equals("Nome atualizado"),"manutenção de usuário preserva login e senha");
            rejeitar(() -> us.excluir(admin,admin.getId()),"administrador não pode excluir a própria conta");
            us.excluir(admin,a.getId());
            verificar(ud.buscarPorId(a.getId()).isEmpty() && td.listar(a.getId(),FiltroTarefa.todos()).isEmpty(),"exclusão de aluno remove sua árvore de dados");
            verificar(us.listar(admin).stream().anyMatch(u -> u.getId()==b.getId()),"exclusão preserva outro aluno");
            verificar(es.relatorio((Aluno)ud.buscarPorId(b.getId()).orElseThrow()).total()==1,"relatório permanece isolado entre alunos");
            System.out.println("APROVADO: " + verificacoes + " verificações com PostgreSQL real.");
        } finally {
            for (int id : criados) ud.excluir(id);
            java.util.Arrays.fill(senha,'\0');
        }
    }
}
