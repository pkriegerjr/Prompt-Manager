-- Adds the prompt description field used by FrontEnd/pages/PagUsuario.html.
-- Run this only on existing databases created before ars_database_v2.sql 2.1.

USE ars_database;

ALTER TABLE prompts
  ADD COLUMN IF NOT EXISTS descricao VARCHAR(255) NULL AFTER conteudo;
