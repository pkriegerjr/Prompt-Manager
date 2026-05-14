-- ============================================================
--  ARS PROMPT — Script de criação do banco de dados
--  Banco:    ars_database
--  Versão:   1.0
--  SGBD:     MySQL 8.x (compatível com XAMPP)
-- ============================================================

-- ── Criar e selecionar o banco ───────────────────────────────
CREATE DATABASE IF NOT EXISTS ars_database
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ars_database;


-- ============================================================
--  TABELA: categorias
--  Precisa existir antes de `prompts` (FK)
-- ============================================================
CREATE TABLE IF NOT EXISTS categorias (
  id         INT          NOT NULL AUTO_INCREMENT,
  nome       VARCHAR(80)  NOT NULL,
  descricao  VARCHAR(255)     NULL,

  PRIMARY KEY (id),
  UNIQUE KEY uq_categorias_nome (nome)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  TABELA: usuarios
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
  id          INT           NOT NULL AUTO_INCREMENT,
  username    VARCHAR(60)   NOT NULL,
  email       VARCHAR(150)  NOT NULL,
  -- Armazene SEMPRE o hash da senha (ex: BCrypt), nunca texto puro
  password    VARCHAR(255)  NOT NULL,
  criado_em   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uq_usuarios_email    (email),
  UNIQUE KEY uq_usuarios_username (username)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  TABELA: prompts
-- ============================================================
CREATE TABLE IF NOT EXISTS prompts (
  id            INT           NOT NULL AUTO_INCREMENT,
  usuario_id    INT           NOT NULL,
  categoria_id  INT               NULL,           -- opcional
  titulo        VARCHAR(150)  NOT NULL,
  conteudo      TEXT          NOT NULL,
  criado_em     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  -- Chaves estrangeiras
  CONSTRAINT fk_prompts_usuario
    FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,

  CONSTRAINT fk_prompts_categoria
    FOREIGN KEY (categoria_id)
    REFERENCES categorias (id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,

  -- Índices para consultas frequentes
  INDEX idx_prompts_usuario   (usuario_id),
  INDEX idx_prompts_categoria (categoria_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  TABELA: historico_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS historico_logs (
  id          INT           NOT NULL AUTO_INCREMENT,
  usuario_id  INT               NULL,             -- NULL se usuário foi deletado
  acao        ENUM(
                'CADASTRO',
                'LOGIN',
                'LOGOUT',
                'CRIAR_PROMPT',
                'EDITAR_PROMPT',
                'DELETAR_PROMPT',
                'ERRO_LOGIN'
              )             NOT NULL,
  detalhes    VARCHAR(500)      NULL,             -- info extra (ex: título do prompt)
  feito_em    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  CONSTRAINT fk_logs_usuario
    FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id)
    ON DELETE SET NULL                            -- log sobrevive mesmo se usuário sumir
    ON UPDATE CASCADE,

  INDEX idx_logs_usuario (usuario_id),
  INDEX idx_logs_acao    (acao),
  INDEX idx_logs_data    (feito_em)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  DADOS INICIAIS — Categorias padrão
-- ============================================================
INSERT INTO categorias (nome, descricao) VALUES
  ('Geral',        'Prompts de uso geral, sem categoria específica'),
  ('Programação',  'Geração de código, debug, explicações técnicas'),
  ('Escrita',      'Redação, revisão, criação de textos e artigos'),
  ('Análise',      'Análise de dados, documentos e argumentos'),
  ('Criatividade', 'Histórias, brainstorming e ideias criativas'),
  ('Estudo',       'Resumos, flashcards e auxílio acadêmico');


-- ============================================================
--  VERIFICAÇÃO FINAL — lista as tabelas criadas
-- ============================================================
SELECT
  table_name          AS 'Tabela',
  table_rows          AS 'Linhas (aprox.)',
  create_time         AS 'Criada em'
FROM information_schema.tables
WHERE table_schema = 'ars_database'
ORDER BY table_name;
