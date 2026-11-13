# Diagramas da implementação

O GitHub renderiza os blocos Mermaid deste arquivo. Os diagramas mostram o domínio e os vínculos do banco; as camadas DAO, service, view e util permanecem separadas.

## Classes de domínio

```mermaid
classDiagram
    direction TB
    class Usuario {
        <<abstract>>
        -int id
        -String nome
        -String email
        -String senhaHash
        +autenticar(char[] senha) boolean
        +exibirResumo() String
    }
    class Aluno {
        -String matricula
        -String curso
        -List~Materia~ materias
        +adicionarMateria(Materia materia)
        +exibirResumo() String
    }
    class Administrador {
        -String cargo
        +exibirResumo() String
    }
    class Materia {
        -int id
        -int idAluno
        -String nome
        -String descricao
        -List~Tarefa~ tarefas
        +adicionarTarefa(Tarefa tarefa)
        +calcularProgresso() double
    }
    class Tarefa {
        -int id
        -int idMateria
        -String titulo
        -String descricao
        -LocalDate dataLimite
        -Prioridade prioridade
        -StatusTarefa status
        +alterarStatus(StatusTarefa status)
        +estaAtrasada() boolean
    }
    Usuario <|-- Aluno
    Usuario <|-- Administrador
    Aluno "1" *-- "0..*" Materia : organiza
    Materia "1" *-- "0..*" Tarefa : contem
```

`Prioridade`: BAIXA, MEDIA, ALTA. `StatusTarefa`: PENDENTE, EM_ANDAMENTO, CONCLUIDA.

## Banco PostgreSQL — esquema educaplan

```mermaid
erDiagram
    USUARIO ||--o| ALUNO : perfil
    USUARIO ||--o| ADMINISTRADOR : perfil
    ALUNO ||--o{ MATERIA : possui
    MATERIA ||--o{ TAREFA : contem
    USUARIO {
        int id_usuario PK
        varchar nome
        varchar email UK
        varchar senha_hash
        varchar tipo
    }
    ALUNO {
        int id_usuario PK,FK
        varchar tipo
        varchar matricula
        varchar curso
    }
    ADMINISTRADOR {
        int id_usuario PK,FK
        varchar tipo
        varchar cargo
    }
    MATERIA {
        int id_materia PK
        int id_aluno FK
        varchar nome
        varchar descricao
    }
    TAREFA {
        int id_tarefa PK
        int id_materia FK
        varchar titulo
        varchar descricao
        date data_limite
        varchar prioridade
        varchar status
    }
```

O código grava usuário e subclasse na mesma transação. As FKs compostas `(id_usuario, tipo)` e os `CHECK`s nas subclasses impedem perfil incompatível. Cada usuário criado pela aplicação possui um único perfil. A exclusão em cascata preserva o ciclo de vida das composições.
