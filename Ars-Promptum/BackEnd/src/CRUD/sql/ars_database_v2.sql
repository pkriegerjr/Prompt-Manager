-- ============================================================
--  ARS PROMPT — Script de criação do banco de dados
--  Banco:    ars_database
--  Versão:   2.0  (com suporte a administrador)
--  SGBD:     MySQL 8.x (compatível com XAMPP / MySQL Workbench)
-- ============================================================

CREATE DATABASE IF NOT EXISTS ars_database
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ars_database;


-- ============================================================
--  TABELA: admins
--  Login separado dos usuários comuns.
--  ⚠️  Armazene SEMPRE o hash da senha (BCrypt), nunca texto puro.
-- ============================================================
CREATE TABLE IF NOT EXISTS admins (
  id         INT          NOT NULL AUTO_INCREMENT,
  username   VARCHAR(60)  NOT NULL,
  email      VARCHAR(150) NOT NULL,
  password   VARCHAR(255) NOT NULL,   -- hash BCrypt
  criado_em  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uq_admins_email    (email),
  UNIQUE KEY uq_admins_username (username)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  TABELA: categorias
--  Criada antes de `prompts` por causa da FK.
-- ============================================================
CREATE TABLE IF NOT EXISTS categorias (
  id        INT          NOT NULL AUTO_INCREMENT,
  nome      VARCHAR(80)  NOT NULL,
  descricao VARCHAR(255)     NULL,

  PRIMARY KEY (id),
  UNIQUE KEY uq_categorias_nome (nome)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  TABELA: usuarios
--  `ativo`  — admin pode desativar/reativar a conta.
--  `role`   — reservado para expansão futura (moderador etc).
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
  id         INT          NOT NULL AUTO_INCREMENT,
  username   VARCHAR(60)  NOT NULL,
  email      VARCHAR(150) NOT NULL,
  password   VARCHAR(255) NOT NULL,   -- hash BCrypt
  ativo      TINYINT(1)   NOT NULL DEFAULT 1,   -- 1 = ativo | 0 = desativado
  role       ENUM('usuario', 'moderador')
                          NOT NULL DEFAULT 'usuario',
  criado_em  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uq_usuarios_email    (email),
  UNIQUE KEY uq_usuarios_username (username),
  INDEX      idx_usuarios_ativo   (ativo)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  TABELA: prompts
-- ============================================================
CREATE TABLE IF NOT EXISTS prompts (
  id            INT          NOT NULL AUTO_INCREMENT,
  usuario_id    INT          NOT NULL,
  categoria_id  INT              NULL,   -- opcional
  titulo        VARCHAR(150) NOT NULL,
  conteudo      TEXT         NOT NULL,
  criado_em     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  CONSTRAINT fk_prompts_usuario
    FOREIGN KEY (usuario_id)  REFERENCES usuarios  (id)
    ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT fk_prompts_categoria
    FOREIGN KEY (categoria_id) REFERENCES categorias (id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  INDEX idx_prompts_usuario   (usuario_id),
  INDEX idx_prompts_categoria (categoria_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  TABELA: historico_logs
--  Registra ações de usuários E de admins.
--  `admin_id`   — preenchido quando a ação é de um admin.
--  `usuario_id` — preenchido quando a ação é de um usuário comum.
--  Ambos aceitam NULL: log sobrevive mesmo se a conta for deletada.
-- ============================================================
CREATE TABLE IF NOT EXISTS historico_logs (
  id          INT          NOT NULL AUTO_INCREMENT,
  usuario_id  INT              NULL,
  admin_id    INT              NULL,
  acao        ENUM(
                -- Usuário
                'CADASTRO',
                'LOGIN',
                'LOGOUT',
                'ERRO_LOGIN',
                'CRIAR_PROMPT',
                'EDITAR_PROMPT',
                'DELETAR_PROMPT',
                -- Admin
                'ADMIN_LOGIN',
                'ADMIN_LOGOUT',
                'ATIVAR_CONTA',
                'DESATIVAR_CONTA',
                'EDITAR_USUARIO',
                'ADMIN_EDITAR_PROMPT',
                'ADMIN_DELETAR_PROMPT',
                'CRIAR_CATEGORIA',
                'EDITAR_CATEGORIA',
                'DELETAR_CATEGORIA'
              )             NOT NULL,
  detalhes    VARCHAR(500)      NULL,   -- ex: "Prompt: Como usar JDBC"
  feito_em    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  CONSTRAINT fk_logs_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  CONSTRAINT fk_logs_admin
    FOREIGN KEY (admin_id) REFERENCES admins (id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  INDEX idx_logs_usuario (usuario_id),
  INDEX idx_logs_admin   (admin_id),
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
--  DADOS INICIAIS — Admin padrão
--  ⚠️  IMPORTANTE: a senha abaixo é "admin123" em texto puro
--      apenas para o primeiro acesso.
--      Troque IMEDIATAMENTE pelo hash BCrypt via código Java
--      antes de usar em qualquer ambiente real.
--      Hash BCrypt de "admin123":
--      $2a$12$pCxi1iBMQjJVOdfsyomtk.Ej9CiNV8K3aFSCRiEDCfQZNOFBBGWua
-- ============================================================
INSERT INTO admins (username, email, password) VALUES
  ('admin', 'admin@arsprompt.local',
   '$2a$12$pCxi1iBMQjJVOdfsyomtk.Ej9CiNV8K3aFSCRiEDCfQZNOFBBGWua');


-- ============================================================
--  VERIFICAÇÃO FINAL
-- ============================================================
SELECT
  table_name AS 'Tabela',
  create_time AS 'Criada em'
FROM information_schema.tables
WHERE table_schema = 'ars_database'
ORDER BY table_name;