#!/bin/bash
echo "========================================"
echo "  Ars Prompt — Compilando e rodando..."
echo "========================================"

cd "$(dirname "$0")"

mkdir -p out

echo "[1/2] Compilando..."
javac -encoding UTF-8 -cp "lib/*" -d out \
  src/main/EmailException.java \
  src/main/SenhaException.java \
  src/main/model/Usuario.java \
  src/main/model/Prompt.java \
  src/main/model/Categoria.java \
  src/main/DatabaseConnection.java \
  src/main/dao/UsuarioDAO.java \
  src/main/dao/PromptDAO.java \
  src/main/dao/CategoriaDAO.java \
  src/main/dao/LogDAO.java \
  src/main/App.java

if [ $? -ne 0 ]; then
  echo ""
  echo "[ERRO] Falha na compilacao."
  echo "Dica: o arquivo mysql-connector-j.jar deve estar em BackEnd/lib/"
  exit 1
fi

echo "[2/2] Iniciando servidor..."
echo ""
java -cp "out:lib/*" App
