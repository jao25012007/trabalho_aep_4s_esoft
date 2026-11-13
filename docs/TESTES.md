# Testes de integração

A suíte verifica cadastro e login, polimorfismo, CRUD de matérias e tarefas, filtros, relatório, isolamento entre alunos e persistência em PostgreSQL.

## Repetir em banco separado

Crie um banco PostgreSQL separado chamado **educaplan_teste**, acessível com o usuário de teste. Não use os dados da aplicação/apresentação. A suíte cria o esquema/tabelas se necessário, cadastra registros próprios e os remove ao final. Ela não utiliza banco simulado.

No Linux, a partir da raiz do projeto:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/educaplan_teste'
export DB_USER='SEU_USUARIO'
export DB_PASSWORD='SUA_SENHA'
export EDUCAPLAN_TEST_DB=SIM
bash scripts/testar.sh
```

No Windows PowerShell:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/educaplan_teste'
$env:DB_USER='SEU_USUARIO'
$env:DB_PASSWORD='SUA_SENHA'
$env:EDUCAPLAN_TEST_DB='SIM'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/testar.ps1
```

A saída final esperada é `APROVADO: 47 verificações com PostgreSQL real.` Em caso de falha, a suíte encerra com erro. Por proteção contra configuração acidental, ela exige a variável `EDUCAPLAN_TEST_DB=SIM` e o nome exato `educaplan_teste` na URL. Essas variáveis sobrepõem o `.env`.

Após o teste, feche esse terminal ou remova as variáveis de teste antes de abrir a aplicação conectada ao Supabase.
