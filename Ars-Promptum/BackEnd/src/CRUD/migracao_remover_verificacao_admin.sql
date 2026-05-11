-- ============================================================
--  Remove verificação de email da tabela admins
--  Execute no MySQL Workbench
-- ============================================================
USE ars_database;

ALTER TABLE admins
  DROP COLUMN IF EXISTS token_verificacao,
  DROP COLUMN IF EXISTS token_expira;

-- Marca todos os admins existentes como verificados
UPDATE admins SET verificado = 1;

SELECT 'Migracao concluida!' AS status;
