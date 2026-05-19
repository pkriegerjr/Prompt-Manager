@echo off
echo ========================================
echo   Ars Promptum - Maven Wrapper
echo ========================================

cd /d "%~dp0"
cd ..

if "%JAVA_HOME%"=="" (
  echo.
  echo [ERRO] JAVA_HOME nao esta configurado.
  echo Configure JAVA_HOME apontando para seu JDK, por exemplo:
  echo C:\Program Files\Java\jdk-21.0.11
  pause
  exit /b 1
)

if not exist mvnw.cmd (
  echo.
  echo [ERRO] Maven Wrapper nao encontrado.
  echo Execute: mvn wrapper:wrapper "-Dmaven=3.9.16" "-Dtype=bin"
  pause
  exit /b 1
)

echo [1/1] Compilando e iniciando servidor via Maven Wrapper...
echo.
mvnw.cmd clean compile exec:java

pause
