package br.edu.educaplan.view;

import br.edu.educaplan.model.*;
import br.edu.educaplan.service.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.Scanner;

public final class Menu {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final java.io.Console console = System.console();
    // O console usa a codificação nativa correta no Windows. Não misture seu
    // leitor com Scanner(System.in): ambos podem consumir/bufferizar a entrada.
    private final Scanner entrada = console == null ? new Scanner(System.in, codificacaoEntrada()) : null;
    private final UsuarioService usuarios;
    private final EstudoService estudos;
    public Menu(UsuarioService usuarios, EstudoService estudos) { this.usuarios = usuarios; this.estudos = estudos; }
    public void iniciar() {
        while (true) {
            System.out.println("\n=== EDUCAPLAN | Organização dos estudos ===\n1. Entrar\n2. Cadastrar aluno\n0. Sair");
            int opcao = inteiro("Opção: ");
            if (opcao == 0) return;
            executar(() -> {
                switch (opcao) {
                    case 1 -> entrar();
                    case 2 -> cadastrarAluno();
                    default -> System.out.println("Opção inválida.");
                }
            });
        }
    }
    public void criarAdministradorInicial() throws SQLException {
        System.out.println("=== PRIMEIRO ADMINISTRADOR ===");
        String nome = ler("Nome: "), email = ler("E-mail: "), cargo = ler("Cargo: ");
        char[] senha = senha();
        try { usuarios.criarAdministradorInicial(nome,email,senha,cargo); System.out.println("Administrador criado. Execute novamente para entrar."); }
        finally { Arrays.fill(senha,'\0'); }
    }
    private void cadastrarAluno() throws SQLException {
        String nome = ler("Nome: "), email = ler("E-mail: "), matricula = ler("Matrícula: "), curso = ler("Curso: ");
        char[] senha = senha();
        try { usuarios.cadastrarAluno(nome,email,senha,matricula,curso); System.out.println("Aluno cadastrado. Você já pode entrar."); }
        finally { Arrays.fill(senha,'\0'); }
    }
    private void entrar() throws SQLException {
        String email = ler("E-mail: "); char[] senha = senha(); Usuario usuario;
        try { usuario = usuarios.autenticar(email,senha); }
        finally { Arrays.fill(senha,'\0'); }
        // A referência é da classe abstrata; a implementação depende do perfil concreto.
        System.out.println("\n" + usuario.exibirResumo());
        if (usuario instanceof Aluno aluno) menuAluno(aluno);
        else menuAdministrador(usuario);
    }
    private void menuAluno(Aluno aluno) {
        while (true) {
            System.out.println("\n=== MENU DO ALUNO ===\n1. Gerenciar matérias\n2. Gerenciar tarefas\n3. Filtrar tarefas\n4. Relatório de progresso\n0. Sair da conta");
            int opcao = inteiro("Opção: "); if (opcao == 0) return;
            executar(() -> {
                switch (opcao) {
                    case 1 -> menuMaterias(aluno);
                    case 2 -> menuTarefas(aluno);
                    case 3 -> mostrarTarefas(aluno,lerFiltro());
                    case 4 -> mostrarRelatorio(aluno);
                    default -> System.out.println("Opção inválida.");
                }
            });
        }
    }
    private void menuMaterias(Aluno aluno) {
        while (true) {
            System.out.println("\n=== MATÉRIAS ===\n1. Cadastrar\n2. Listar\n3. Atualizar\n4. Excluir\n0. Voltar");
            int opcao = inteiro("Opção: "); if (opcao == 0) return;
            executar(() -> {
                switch (opcao) {
                    case 1 -> {
                        int id = estudos.criarMateria(aluno,ler("Nome: "),ler("Descrição (opcional): "));
                        System.out.println("Matéria cadastrada. ID: " + id);
                    }
                    case 2 -> mostrarMaterias(aluno);
                    case 3 -> {
                        mostrarMaterias(aluno); int id = inteiro("ID da matéria: ");
                        estudos.atualizarMateria(aluno,id,ler("Novo nome: "),ler("Nova descrição (opcional): "));
                        System.out.println("Matéria atualizada.");
                    }
                    case 4 -> {
                        mostrarMaterias(aluno); int id = inteiro("ID da matéria: ");
                        if (confirmar("Excluir a matéria e TODAS as suas tarefas?")) { estudos.excluirMateria(aluno,id); System.out.println("Matéria excluída."); }
                    }
                    default -> System.out.println("Opção inválida.");
                }
            });
        }
    }
    private void mostrarMaterias(Aluno aluno) throws SQLException {
        var lista = estudos.listarMaterias(aluno);
        if (lista.isEmpty()) System.out.println("Nenhuma matéria cadastrada.");
        for (Materia m : lista) System.out.println("ID " + m.getId() + " | " + m.getNome() + " | " + m.getDescricao());
    }
    private void menuTarefas(Aluno aluno) {
        while (true) {
            System.out.println("\n=== TAREFAS ===\n1. Cadastrar\n2. Listar\n3. Atualizar\n4. Alterar status\n5. Excluir\n0. Voltar");
            int opcao = inteiro("Opção: "); if (opcao == 0) return;
            executar(() -> {
                switch (opcao) {
                    case 1 -> {
                        if (estudos.listarMaterias(aluno).isEmpty()) { System.out.println("Cadastre uma matéria primeiro."); return; }
                        mostrarMaterias(aluno); int materia = inteiro("ID da matéria: ");
                        String titulo = ler("Título: "), descricao = ler("Descrição (opcional): "); LocalDate prazo = data(false);
                        Prioridade prioridade = escolher(Prioridade.values(),false); StatusTarefa status = escolher(StatusTarefa.values(),false);
                        int id = estudos.criarTarefa(aluno,materia,titulo,descricao,prazo,prioridade,status);
                        System.out.println("Tarefa cadastrada. ID: " + id);
                    }
                    case 2 -> mostrarTarefas(aluno,FiltroTarefa.todos());
                    case 3 -> {
                        mostrarTarefas(aluno,FiltroTarefa.todos()); int id = inteiro("ID da tarefa: ");
                        Tarefa atual = estudos.buscarTarefa(aluno,id);
                        System.out.println("Editando: " + atual.getTitulo() + ". Informe os novos valores.");
                        String titulo = ler("Título: "), descricao = ler("Descrição (opcional): "); LocalDate prazo = data(false);
                        Prioridade prioridade = escolher(Prioridade.values(),false); StatusTarefa status = escolher(StatusTarefa.values(),false);
                        estudos.atualizarTarefa(aluno,id,titulo,descricao,prazo,prioridade,status); System.out.println("Tarefa atualizada.");
                    }
                    case 4 -> {
                        mostrarTarefas(aluno,FiltroTarefa.todos()); int id = inteiro("ID da tarefa: ");
                        estudos.alterarStatus(aluno,id,escolher(StatusTarefa.values(),false)); System.out.println("Status e progresso atualizados.");
                    }
                    case 5 -> {
                        mostrarTarefas(aluno,FiltroTarefa.todos()); int id = inteiro("ID da tarefa: ");
                        if (confirmar("Excluir esta tarefa?")) { estudos.excluirTarefa(aluno,id); System.out.println("Tarefa excluída."); }
                    }
                    default -> System.out.println("Opção inválida.");
                }
            });
        }
    }
    private FiltroTarefa lerFiltro() {
        System.out.println("Combine os filtros desejados. Enter deixa o campo sem filtro.");
        String materia = ler("ID da matéria: "); Integer id = null;
        if (!materia.isBlank()) {
            try { id = Integer.valueOf(materia); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("ID de matéria inválido."); }
        }
        LocalDate prazo = data(true);
        return new FiltroTarefa(id,prazo,escolher(Prioridade.values(),true),escolher(StatusTarefa.values(),true));
    }
    private void mostrarTarefas(Aluno aluno, FiltroTarefa filtro) throws SQLException {
        var tarefas = estudos.listarTarefas(aluno,filtro);
        if (tarefas.isEmpty()) System.out.println("Nenhuma tarefa encontrada.");
        for (Tarefa t : tarefas) {
            System.out.printf("ID %d | Matéria %d | %s | Prazo %s | %s | %s%s%n",t.getId(),t.getIdMateria(),t.getTitulo(),DATA.format(t.getDataLimite()),
                t.getPrioridade(),t.getStatus(),t.estaAtrasada() ? " | ATRASADA" : "");
            if (!t.getDescricao().isBlank()) System.out.println("  " + t.getDescricao());
        }
    }
    private void mostrarRelatorio(Aluno aluno) throws SQLException {
        Relatorio r = estudos.relatorio(aluno);
        System.out.println("\n=== PROGRESSO DOS ESTUDOS ===");
        System.out.printf("Matérias: %d | Tarefas: %d | Pendentes: %d | Em andamento: %d | Concluídas: %d | Atrasadas: %d%n",
            r.materias().size(),r.total(),r.pendentes(),r.emAndamento(),r.concluidas(),r.atrasadas());
        System.out.printf("Conclusão geral: %.1f%%%n",r.percentualConclusao());
        for (Materia m : r.materias()) System.out.printf("%s: %d tarefas | %.1f%% concluído%n",m.getNome(),m.getTarefas().size(),m.calcularProgresso());
    }
    private void menuAdministrador(Usuario administrador) {
        while (true) {
            System.out.println("\n=== ADMINISTRAÇÃO DE USUÁRIOS ===\n1. Listar\n2. Cadastrar aluno\n3. Cadastrar administrador\n4. Atualizar usuário\n5. Excluir usuário\n0. Sair da conta");
            int opcao = inteiro("Opção: "); if (opcao == 0) return;
            executar(() -> {
                switch (opcao) {
                    case 1 -> {
                        for (Usuario u : usuarios.listar(administrador)) System.out.println("ID " + u.getId() + " | " + u.exibirResumo() + " | " + u.getEmail());
                    }
                    case 2 -> cadastrarAluno();
                    case 3 -> {
                        String nome = ler("Nome: "), email = ler("E-mail: "), cargo = ler("Cargo: "); char[] senha = senha();
                        try { usuarios.cadastrarAdministrador(administrador,nome,email,senha,cargo); System.out.println("Administrador cadastrado."); }
                        finally { Arrays.fill(senha,'\0'); }
                    }
                    case 4 -> {
                        Usuario u = usuarios.buscar(administrador,inteiro("ID do usuário: "));
                        String nome = ler("Novo nome: "), email = ler("Novo e-mail: "); Usuario atualizado;
                        if (u instanceof Aluno) atualizado = new Aluno(u.getId(),nome,email,u.getSenhaHash(),ler("Matrícula: "),ler("Curso: "));
                        else atualizado = new Administrador(u.getId(),nome,email,u.getSenhaHash(),ler("Cargo: "));
                        usuarios.atualizar(administrador,atualizado); System.out.println("Usuário atualizado.");
                    }
                    case 5 -> {
                        int id = inteiro("ID do usuário: ");
                        if (confirmar("Excluir o usuário e todas as matérias e tarefas dele?")) { usuarios.excluir(administrador,id); System.out.println("Usuário excluído."); }
                    }
                    default -> System.out.println("Opção inválida.");
                }
            });
        }
    }
    private static java.nio.charset.Charset codificacaoEntrada() {
        String nome = System.getProperty("stdin.encoding", System.getProperty("native.encoding", "UTF-8"));
        return java.nio.charset.Charset.forName(nome);
    }
    private String lerLinha(String mensagem) {
        if (console != null) {
            String valor = console.readLine("%s", mensagem);
            if (valor == null) throw new java.util.NoSuchElementException();
            return valor;
        }
        System.out.print(mensagem);
        return entrada.nextLine();
    }
    private String ler(String mensagem) { return lerLinha(mensagem).strip(); }
    private int inteiro(String mensagem) {
        while (true) {
            try { return Integer.parseInt(ler(mensagem)); }
            catch (NumberFormatException e) { System.out.println("Digite um número inteiro válido."); }
        }
    }
    private boolean confirmar(String mensagem) { return ler(mensagem + " [s/N]: ").equalsIgnoreCase("s"); }
    private char[] senha() {
        if (console != null) {
            char[] valor = console.readPassword("Senha (mínimo 8 caracteres): ");
            if (valor == null) throw new java.util.NoSuchElementException();
            return valor;
        }
        return lerLinha("Senha (visível neste terminal, mínimo 8 caracteres): ").toCharArray();
    }
    private LocalDate data(boolean opcional) {
        String valor = ler(opcional ? "Prazo até (dd/mm/aaaa, Enter para todos): " : "Prazo (dd/mm/aaaa): ");
        if (opcional && valor.isEmpty()) return null;
        try { return LocalDate.parse(valor,DATA); }
        catch (java.time.format.DateTimeParseException e) { throw new IllegalArgumentException("Data inválida. Use dd/mm/aaaa, por exemplo 20/11/2026."); }
    }
    private <E extends Enum<E>> E escolher(E[] opcoes, boolean opcional) {
        for (int i = 0; i < opcoes.length; i++) System.out.printf("%d. %s%n",i + 1,opcoes[i]);
        String valor = ler(opcional ? "Escolha (Enter para todos): " : "Escolha: ");
        if (opcional && valor.isEmpty()) return null;
        try {
            int indice = Integer.parseInt(valor) - 1;
            if (indice >= 0 && indice < opcoes.length) return opcoes[indice];
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("Escolha uma das opções exibidas.");
    }
    private void executar(Acao acao) {
        try { acao.executar(); }
        catch (IllegalArgumentException | SQLException e) { System.out.println("Atenção: " + mensagemErro(e)); }
    }
    public static String mensagemErro(Exception e) {
        if (e instanceof SQLException sql) {
            String estado = sql.getSQLState();
            if ("23505".equals(estado)) return "Já existe usuário com esse e-mail.";
            if ("22021".equals(estado)) return "O banco rejeitou um caractere inválido no cadastro. "
                + "Digite o campo novamente. Detalhe do banco: " + sql.getMessage();
            if ("42P01".equals(estado) || "3F000".equals(estado)) return "Execute database/educaplan.sql no banco configurado.";
            if ("28P01".equals(estado) || "28000".equals(estado)) return "Credenciais do banco inválidas. Confira DB_USER e DB_PASSWORD.";
            if ("42501".equals(estado)) return "O usuário do banco não tem permissão para essa operação.";
            return "Falha ao acessar o banco (estado " + estado + "). Confira conexão, credenciais e script SQL. Nenhuma confirmação de sucesso foi emitida.";
        }
        return e.getMessage();
    }
    @FunctionalInterface private interface Acao { void executar() throws SQLException; }
}
