# EducaPlan — AEP, segunda etapa

Aplicação Java de terminal para organizar matérias e tarefas, consultar prazos e acompanhar o progresso dos estudos. Os dados são gravados no PostgreSQL do Supabase via JDBC.

## Como executar

1. Instale um **JDK 17 ou superior** e confira se `java -version` e `javac -version` funcionam no terminal.
2. Para usar o banco de avaliação, coloque o arquivo `.env` recebido em privado na raiz do projeto, ao lado de `executar.bat`. Para preparar um banco próprio, execute [database/educaplan.sql](database/educaplan.sql) no SQL Editor, copie `.env.example` para `.env` e preencha `DB_URL`, `DB_USER` e `DB_PASSWORD`.
3. No Windows, abra `executar.bat`. No Linux, execute `bash scripts/executar.sh`.

O driver JDBC já está na pasta `lib/`. Com a conexão configurada, o programa acessa o banco diretamente.

## Como usar

1. No menu inicial, escolha **Cadastrar aluno**. Depois selecione **Entrar** com o e-mail e a senha cadastrados.
2. Em **Gerenciar matérias**, cadastre a primeira matéria.
3. Em **Gerenciar tarefas**, vincule a tarefa à matéria e informe o prazo, a prioridade e o status.
4. Use **Filtrar tarefas** e **Relatório de progresso** para acompanhar as atividades.

## Onde encontrar cada parte

| O que você procura | Arquivo ou pasta |
| --- | --- |
| Como o banco foi montado, diagrama e consultas SQL | [docs/ESQUEMA_BANCO.md](docs/ESQUEMA_BANCO.md) |
| Script que cria as tabelas | [database/educaplan.sql](database/educaplan.sql) |
| Código-fonte Java | [src/main/java/br/edu/educaplan/](src/main/java/br/edu/educaplan/) |
| Configuração do Supabase e solução de erros | [docs/COMO_EXECUTAR.md](docs/COMO_EXECUTAR.md) |
| Diagramas da implementação | [docs/DIAGRAMAS.md](docs/DIAGRAMAS.md) |
| Testes | [docs/TESTES.md](docs/TESTES.md) |
| Requisitos da AEP e roteiro de apresentação | [Checklist](docs/CHECKLIST_ENTREGA.md) · [Roteiro](docs/ROTEIRO_APRESENTACAO.md) |
