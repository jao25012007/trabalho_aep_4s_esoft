# Executar o EducaPlan no Windows com Supabase

## 1. Abrir o projeto e conferir o Java

Abra a pasta do projeto completo. A raiz contém `executar.bat`, `README.md`, `src/`, `database/` e `lib/`.

Instale um JDK 17 ou superior. Abra um novo Prompt de Comando e execute:

```bat
java -version
javac -version
```

Os dois precisam mostrar uma versão 17 ou superior. Se só `java` funcionar, provavelmente existe apenas um runtime ou falta configurar o JDK no PATH.

## 2. Criar as tabelas em um banco novo

Se recebeu o `.env` do banco de avaliação já preparado, coloque-o na raiz e vá para a etapa 4.

1. Entre no Supabase e abra o projeto do grupo. Se ainda não existir, crie um projeto e guarde a senha do **banco de dados**.
2. Abra o **SQL Editor** e uma consulta nova.
3. Abra o arquivo `database/educaplan.sql` em um editor de texto.
4. Copie o conteúdo inteiro para o SQL Editor e execute.
5. As tabelas `usuario`, `aluno`, `administrador`, `materia` e `tarefa` ficam no esquema **educaplan**. Se estiver olhando somente o esquema `public`, troque o esquema selecionado ou consulte por SQL.

Para conferir:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'educaplan'
ORDER BY table_name;
```

Não é necessário cadastrar usuários manualmente pelo painel. Faça isso pelo programa.

## 3. Configurar a conexão

No painel do projeto, abra **Connect** e copie os parâmetros de conexão PostgreSQL. Para uma conexão local de longa duração, o **Session pooler** costuma ser conveniente, inclusive em redes IPv4. Utilize exatamente o host, a porta, o banco e o usuário exibidos no painel; não adivinhe a região do projeto. Os rótulos do painel podem variar.

Copie `.env.example` e renomeie a cópia para **`.env`**. No Windows, ative a exibição das extensões dos arquivos para evitar o nome `.env.txt`.

Preencha:

```dotenv
DB_URL=jdbc:postgresql://HOST_DO_PAINEL:PORTA_DO_PAINEL/postgres?sslmode=require
DB_USER=USUARIO_DO_PAINEL
DB_PASSWORD=SENHA_DO_BANCO
```

- O endereço deve começar com `jdbc:postgresql://`.
- `HOST_DO_PAINEL`, `PORTA_DO_PAINEL` e `USUARIO_DO_PAINEL` são textos ilustrativos: substitua-os pelos valores reais.
- A senha é a senha do PostgreSQL, e não a senha de login no site, a `anon key` ou a `service_role key`.
- A senha deve ficar em `DB_PASSWORD`, sem precisar colocá-la na URL.
- Se o projeto estiver pausado, retome-o antes de conectar.
- Conexão direta também pode ser usada quando a rede oferecer conectividade adequada ao host informado.

Guarde o arquivo `.env` na raiz do projeto no seu computador.

## 4. Abrir o programa

Abra `executar.bat`. Ele compila o código e abre os menus. Para testar somente a conexão, abra um terminal na pasta do projeto e execute:

```bat
executar.bat --verificar-banco
```

A mensagem esperada é `Conexão e tabelas do EducaPlan verificadas.`

Na tela inicial:

1. Escolha **2. Cadastrar aluno**.
2. Informe nome, e-mail, matrícula, curso e uma senha de 8 a 128 caracteres.
3. Escolha **1. Entrar**, usando o e-mail e a senha cadastrados.
4. Em **Gerenciar matérias**, cadastre uma matéria.
5. Em **Gerenciar tarefas**, cadastre uma tarefa usando o ID da matéria listada.
6. Digite datas no formato **dd/mm/aaaa**. Para prioridade/status, digite o número da opção.

Guarde os IDs exibidos nas listagens para editar ou excluir registros. Os IDs são gerados pelo banco e podem conter intervalos; não precisam começar em 1.

## 5. Criar o primeiro administrador, se necessário

Feche o programa e execute no terminal, na mesma pasta:

```bat
executar.bat --criar-admin
```

Informe os dados solicitados. Execute normalmente, entre com essa conta e teste o menu de manutenção de usuários. Esse comando deixa de aceitar novos administradores quando já existe um. Outros devem ser criados no menu autenticado de administração.

## 6. Solucionar erros comuns

| Mensagem/sintoma | O que conferir |
| --- | --- |
| `javac` não encontrado | Instalação de um JDK e seu PATH; abra outro terminal após instalar |
| `release version 17 not supported` | JDK usado no PATH antigo ou incompleto; confira `javac -version` e use um JDK completo 17+ |
| Configure `DB_URL` | O `.env` está na pasta ao lado do `README.md`, com nome correto e todas as variáveis? |
| Credenciais do banco inválidas | Usuário exato do painel Connect, senha do banco e projeto correto |
| Falha ao acessar o banco | Host/porta, internet, projeto pausado, restrições de rede e opção de conexão correta |
| Execute `database/educaplan.sql` | Execute o SQL no mesmo banco que aparece em `DB_URL` |
| Estado `22021` ou caractere nulo | Reabra o programa e digite o campo novamente. |
| Nenhuma matéria/tarefa | Os registros pertencem à conta autenticada; confira o aluno e os filtros |
| A senha aparece ao digitar | Em alguns terminais/IDEs não há suporte a senha oculta; o programa informa isso antes da entrada |
| A janela fecha após um erro | Execute o `.bat` pelo Prompt e leia a mensagem; não abra o `.ps1` como documento |

## 7. Usar PostgreSQL local como alternativa

Crie um banco chamado `educaplan`, execute o mesmo script SQL e use:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/educaplan
DB_USER=SEU_USUARIO_POSTGRES
DB_PASSWORD=SUA_SENHA_LOCAL
```

O mesmo script prepara as tabelas no banco local.

[Conexão PostgreSQL no Supabase](https://supabase.com/docs/guides/database/connecting-to-postgres) · [Driver JDBC PostgreSQL](https://jdbc.postgresql.org/documentation/use/)
