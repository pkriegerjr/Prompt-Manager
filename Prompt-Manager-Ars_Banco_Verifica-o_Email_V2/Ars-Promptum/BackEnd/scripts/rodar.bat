@echo off
echo ========================================
echo   Ars Prompt — Compilando e rodando...
echo ========================================

cd /d "%~dp0"
cd ..

if not exist out mkdir out

echo [1/2] Compilando...
javac -encoding UTF-8 -cp "libs/*" -sourcepath src/main -d out ^
  src/main/App.java ^
  src/main/EmailException.java ^
  src/main/SenhaException.java

if %errorlevel% neq 0 (
  echo.
  echo [ERRO] Falha na compilacao. Verifique os erros acima.
  echo Dica: os arquivos .jar devem estar em BackEnd/libs/
  pause
  exit /b 1
)

:: Roda o servidor
echo [2/2] Iniciando servidor...
echo.
java -cp "out;libs/*" App
pause
