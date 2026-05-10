@echo off
echo ========================================
echo   Ars Prompt — Compilando e rodando...
echo ========================================

cd /d "%~dp0"

:: Cria pasta out se nao existir
if not exist out mkdir out

:: Compila todos os arquivos Java
echo [1/2] Compilando...
javac -encoding UTF-8 -cp "lib/*" -d out ^
  src/CRUD/EmailException.java ^
  src/CRUD/SenhaException.java ^
  src/CRUD/model/Usuario.java ^
  src/CRUD/model/Prompt.java ^
  src/CRUD/model/Categoria.java ^
  src/CRUD/DatabaseConnection.java ^
  src/CRUD/dao/UsuarioDAO.java ^
  src/CRUD/dao/PromptDAO.java ^
  src/CRUD/dao/CategoriaDAO.java ^
  src/CRUD/dao/LogDAO.java ^
  src/CRUD/App.java

if %errorlevel% neq 0 (
  echo.
  echo [ERRO] Falha na compilacao. Verifique os erros acima.
  echo Dica: o arquivo mysql-connector-j.jar deve estar em BackEnd/lib/
  pause
  exit /b 1
)

:: Roda o servidor
echo [2/2] Iniciando servidor...
echo.
java -cp "out;lib/*" App
pause
