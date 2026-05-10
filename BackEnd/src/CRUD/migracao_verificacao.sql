-- ============================================================
--  ARS PROMPT — Migração: verificação de email
--  Execute no MySQL Workbench ANTES de rodar o novo App.java
-- ============================================================

USE ars_database;

-- Adiciona colunas de verificação em usuarios
ALTER TABLE usuarios
  ADD COLUMN verificado        TINYINT(1)   NOT NULL DEFAULT 0   AFTER ativo,
  ADD COLUMN token_verificacao VARCHAR(6)       NULL              AFTER verificado,
  ADD COLUMN token_expira      DATETIME         NULL              AFTER token_verificacao;

-- Adiciona colunas de verificação em admins
ALTER TABLE admins
  ADD COLUMN verificado        TINYINT(1)   NOT NULL DEFAULT 0   AFTER id,
  ADD COLUMN token_verificacao VARCHAR(6)       NULL              AFTER verificado,
  ADD COLUMN token_expira      DATETIME         NULL              AFTER token_verificacao;

-- Marca usuários/admins já existentes como verificados
-- (quem já estava no sistema não precisa reverificar)
UPDATE usuarios SET verificado = 1;
UPDATE admins   SET verificado = 1;

-- Adiciona ação de verificação no ENUM de logs
ALTER TABLE historico_logs
  MODIFY COLUMN acao ENUM(
    'CADASTRO','LOGIN','LOGOUT','ERRO_LOGIN',
    'CRIAR_PROMPT','EDITAR_PROMPT','DELETAR_PROMPT',
    'ADMIN_LOGIN','ADMIN_LOGOUT',
    'ATIVAR_CONTA','DESATIVAR_CONTA','EDITAR_USUARIO',
    'ADMIN_EDITAR_PROMPT','ADMIN_DELETAR_PROMPT',
    'CRIAR_CATEGORIA','EDITAR_CATEGORIA','DELETAR_CATEGORIA',
    'EMAIL_VERIFICADO','ADMIN_CRIADO'
  ) NOT NULL;

SELECT 'Migracao concluida!' AS status;
USE ars_database;

ALTER TABLE usuarios
  MODIFY COLUMN token_verificacao VARCHAR(36) NULL;

ALTER TABLE admins
  MODIFY COLUMN token_verificacao VARCHAR(36) NULL;


USE ars_database;

UPDATE admins 
SET password = SHA2('admin123', 256)
WHERE email = 'admin@arsprompt.local';

USE ars_database;
UPDATE admins SET password = SHA2('admin123', 256), verificado = 1
WHERE email = 'admin@arsprompt.local';
