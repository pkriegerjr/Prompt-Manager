#!/bin/bash
echo "========================================"
echo "  Ars Prompt — Compilando e rodando..."
echo "========================================"

cd "$(dirname "$0")"

mkdir -p out

echo "[1/2] Compilando..."
javac -encoding UTF-8 -cp "lib/*" -d out \
  src/CRUD/EmailException.java \
  src/CRUD/SenhaException.java \
  src/CRUD/model/Usuario.java \
  src/CRUD/model/Prompt.java \
  src/CRUD/model/Categoria.java \
  src/CRUD/DatabaseConnection.java \
  src/CRUD/dao/UsuarioDAO.java \
  src/CRUD/dao/PromptDAO.java \
  src/CRUD/dao/CategoriaDAO.java \
  src/CRUD/dao/LogDAO.java \
  src/CRUD/App.java

if [ $? -ne 0 ]; then
  echo ""
  echo "[ERRO] Falha na compilacao."
  echo "Dica: o arquivo mysql-connector-j.jar deve estar em BackEnd/lib/"
  exit 1
fi

echo "[2/2] Iniciando servidor..."
echo ""
java -cp "out:lib/*" App
