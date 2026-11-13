package br.edu.educaplan;

import br.edu.educaplan.dao.*;
import br.edu.educaplan.service.*;
import br.edu.educaplan.util.*;
import br.edu.educaplan.view.Menu;

public final class Main {
    private Main() {}
    public static void main(String[] args) {
        try {
            if (args.length > 1 || (args.length == 1 && !args[0].equals("--criar-admin") && !args[0].equals("--verificar-banco")))
                throw new IllegalArgumentException("Opções: --criar-admin ou --verificar-banco (ou nenhuma para abrir os menus).");
            Conexao banco = new Conexao(new Configuracao());
            banco.verificar();
            if (args.length == 1 && args[0].equals("--verificar-banco")) { System.out.println("Conexão e tabelas do EducaPlan verificadas."); return; }
            Menu menu = new Menu(new UsuarioService(new UsuarioDao(banco)),new EstudoService(new MateriaDao(banco),new TarefaDao(banco)));
            if (args.length == 1) menu.criarAdministradorInicial(); else menu.iniciar();
        } catch (java.util.NoSuchElementException e) { System.out.println("\nEntrada encerrada."); }
        catch (java.io.IOException | IllegalArgumentException | java.sql.SQLException e) {
            System.err.println("Não foi possível continuar: " + Menu.mensagemErro(e)); System.exit(1);
        }
    }
}
