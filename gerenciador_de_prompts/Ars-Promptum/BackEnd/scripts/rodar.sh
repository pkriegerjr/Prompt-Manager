#!/bin/bash
echo "========================================"
echo "  Ars Promptum - Maven Wrapper"
echo "========================================"

cd "$(dirname "$0")/.."

if [ -z "${JAVA_HOME:-}" ]; then
  echo ""
  echo "[ERRO] JAVA_HOME nao esta configurado."
  echo "Configure JAVA_HOME apontando para seu JDK."
  exit 1
fi

if [ ! -f "./mvnw" ]; then
  echo ""
  echo "[ERRO] Maven Wrapper nao encontrado."
  echo "Execute: mvn wrapper:wrapper \"-Dmaven=3.9.16\" \"-Dtype=bin\""
  exit 1
fi

echo "[1/1] Compilando e iniciando servidor via Maven Wrapper..."
echo ""
./mvnw clean compile exec:java
