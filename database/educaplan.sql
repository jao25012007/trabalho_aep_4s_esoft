-- EducaPlan: executar uma vez no SQL Editor do Supabase ou com psql.
-- Não apaga dados. O esquema próprio evita expor as tabelas pela API pública.
BEGIN;
CREATE SCHEMA IF NOT EXISTS educaplan;
CREATE TABLE IF NOT EXISTS educaplan.usuario (
    id_usuario INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(120) NOT NULL CHECK (length(trim(nome)) > 0),
    email VARCHAR(160) NOT NULL UNIQUE CHECK (email = lower(email)),
    senha_hash VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ALUNO', 'ADMINISTRADOR')),
    UNIQUE (id_usuario, tipo)
);
CREATE TABLE IF NOT EXISTS educaplan.aluno (
    id_usuario INTEGER PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL DEFAULT 'ALUNO' CHECK (tipo = 'ALUNO'),
    matricula VARCHAR(30) NOT NULL,
    curso VARCHAR(100) NOT NULL,
    FOREIGN KEY (id_usuario, tipo) REFERENCES educaplan.usuario(id_usuario, tipo) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS educaplan.administrador (
    id_usuario INTEGER PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL DEFAULT 'ADMINISTRADOR' CHECK (tipo = 'ADMINISTRADOR'),
    cargo VARCHAR(80) NOT NULL,
    FOREIGN KEY (id_usuario, tipo) REFERENCES educaplan.usuario(id_usuario, tipo) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS educaplan.materia (
    id_materia INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_aluno INTEGER NOT NULL REFERENCES educaplan.aluno(id_usuario) ON DELETE CASCADE,
    nome VARCHAR(100) NOT NULL CHECK (length(trim(nome)) > 0),
    descricao VARCHAR(255) NOT NULL DEFAULT ''
);
CREATE TABLE IF NOT EXISTS educaplan.tarefa (
    id_tarefa INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_materia INTEGER NOT NULL REFERENCES educaplan.materia(id_materia) ON DELETE CASCADE,
    titulo VARCHAR(120) NOT NULL CHECK (length(trim(titulo)) > 0),
    descricao VARCHAR(255) NOT NULL DEFAULT '',
    data_limite DATE NOT NULL,
    prioridade VARCHAR(15) NOT NULL CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA'))
);
CREATE INDEX IF NOT EXISTS ix_materia_aluno ON educaplan.materia(id_aluno);
CREATE INDEX IF NOT EXISTS ix_tarefa_materia ON educaplan.tarefa(id_materia);
CREATE INDEX IF NOT EXISTS ix_tarefa_prazo ON educaplan.tarefa(data_limite);
COMMIT;
