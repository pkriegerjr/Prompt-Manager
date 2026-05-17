#!/bin/bash
echo "========================================"
echo "  Ars Prompt — Compilando e rodando..."
echo "========================================"

cd "$(dirname "$0")/.."

mkdir -p out

echo "[1/2] Compilando..."
javac -encoding UTF-8 -cp "libs/*" -sourcepath src/main -d out \
  src/main/App.java \
  src/main/EmailException.java \
  src/main/SenhaException.java

if [ $? -ne 0 ]; then
  echo ""
  echo "[ERRO] Falha na compilacao."
  echo "Dica: os arquivos .jar devem estar em BackEnd/libs/"
  exit 1
fi

echo "[2/2] Iniciando servidor..."
echo ""
java -cp "out:libs/*" App
