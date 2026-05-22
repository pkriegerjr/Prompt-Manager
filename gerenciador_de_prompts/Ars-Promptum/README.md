# Como Rodar o Ars Promptum

Este projeto roda pelo Maven. O fluxo principal usa o Maven Wrapper do backend, sem depender de uma instalacao global do Maven.

## Requisitos

- JDK 17 ou superior instalado e `JAVA_HOME` configurado.
- MySQL iniciado (via XAMPP ou instalacao local).
- Arquivo `config.env` configurado na pasta `Ars-Promptum`.
- Banco criado com `BackEnd/database/ars_database_v2_2.sql`.

## Configurar JAVA_HOME no Windows

O Maven Wrapper (`.\mvnw.cmd`), os scripts em `BackEnd/scripts` e a execucao do JAR precisam encontrar um JDK valido. O `JAVA_HOME` deve apontar para a pasta raiz do JDK, nao para a pasta `bin`.

Exemplos de caminho valido:

```text
C:\Program Files\Java\jdk-17
C:\Program Files\Java\jdk-21
```

Para configurar apenas o PowerShell atual:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
echo $env:JAVA_HOME
java -version
javac -version
```

Para deixar permanente no Windows:

1. Abra o menu Iniciar e procure por `Editar as variaveis de ambiente da sua conta`.
2. Em `Variaveis de usuario`, crie ou edite `JAVA_HOME` com o caminho da pasta do JDK.
3. Edite a variavel `Path` e adicione `%JAVA_HOME%\bin`.
4. Feche e reabra o PowerShell ou o VS Code.

Depois de reabrir o terminal, valide:

```powershell
echo $env:JAVA_HOME
java -version
javac -version
```

Se `JAVA_HOME` estiver vazio ou apontando para uma pasta incorreta, corrija antes de rodar `.\mvnw.cmd`, `.\scripts\rodar.bat` ou `java -jar`.

O backend sobe em:

```text
http://localhost:8081
```

## Rodar o Backend com Maven Wrapper

No PowerShell, entre na pasta do backend:

```powershell
cd C:\........\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
.\mvnw.cmd clean compile exec:java
```

No Linux/macOS:

```bash
cd /caminho/do/projeto/gerenciador_de_prompts/Ars-Promptum/BackEnd
./mvnw clean compile exec:java
```

## Rodar pelo Script

No Windows:

```powershell
cd C:\........\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
.\scripts\rodar.bat
```

No Linux/macOS:

```bash
cd /caminho/do/projeto/gerenciador_de_prompts/Ars-Promptum/BackEnd
./scripts/rodar.sh
```

Os scripts tambem usam o Maven Wrapper.

## Gerar e Rodar o JAR Executavel

No Windows:

```powershell
cd C:\.........\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
.\mvnw.cmd clean package
java -jar target\ars-promptum-backend-1.0.0-SNAPSHOT.jar
```

No Linux/macOS:

```bash
cd /caminho/do/projeto/gerenciador_de_prompts/Ars-Promptum/BackEnd
./mvnw clean package
java -jar target/ars-promptum-backend-1.0.0-SNAPSHOT.jar
```

Execute o JAR a partir da pasta `BackEnd`, para que o `config.env` em `Ars-Promptum/config.env` seja encontrado corretamente.

## Usar o Maven Pai

Na raiz `gerenciador_de_prompts`, existe um `pom.xml` agregador. Com Maven instalado no `PATH`, rode:
(Não recomendado)

```powershell
cd C:\........\Prompt-Manager\gerenciador_de_prompts
mvn clean package
```

Para executar apenas o modulo do backend:

```powershell
cd C:\......\Prompt-Manager\gerenciador_de_prompts
mvn -pl Ars-Promptum/BackEnd clean package
```
Para abrir o servidor usando esse pom.xml da raiz

```powershell
cd "C:\.....\Prompt-Manager\gerenciador_de_prompts"
mvn -pl Ars-Promptum/BackEnd clean compile exec:java
```

## Rodar os Testes

Os testes automatizados ficam em `BackEnd/src/test/java`.

- `AppSmokeTest`: sobe o Javalin em uma porta dinamica e valida rotas HTTP, frontend estatico, handlers globais e autorizacao por token.
- `SessionTokenTest`: valida emissao, leitura e rejeicao de token malformado/adulterado.

No Windows, usando o Maven Wrapper:

```powershell
cd C:\Users\SONY VAIO\OneDrive\Documentos\vscode\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
.\mvnw.cmd test
```

Para recompilar tudo antes dos testes:

```powershell
.\mvnw.cmd clean test
```

No Linux/macOS:

```bash
cd /caminho/do/projeto/gerenciador_de_prompts/Ars-Promptum/BackEnd
./mvnw test
```

Tambem e possivel rodar pelo Maven pai, caso o Maven esteja instalado no `PATH`:

```powershell
cd C:\Users\SONY VAIO\OneDrive\Documentos\vscode\Prompt-Manager\gerenciador_de_prompts
mvn -pl Ars-Promptum/BackEnd test
```

O comando `clean package` tambem executa os testes antes de gerar o JAR. Os relatorios ficam em `BackEnd/target/surefire-reports`, e a pasta `target/` e ignorada pelo Git.

No estado atual, a suite automatizada nao precisa do MySQL ativo, porque os testes cobrem validacoes e bloqueios que acontecem antes do acesso ao banco. Para validar login real, CRUD de prompts e painel admin com dados persistidos, inicie o MySQL e rode o backend em `http://localhost:8081`.

## Abrir o Frontend

Com o backend rodando, abra no navegador:

```text
http://localhost:8081/
```

O Javalin redireciona a raiz para `http://localhost:8081/pages/index.html` e serve os assets de `FrontEnd/assets`.

Se voce abrir as paginas pelo Live Server do VS Code, normalmente em `http://127.0.0.1:5500`, deixe o backend Javalin rodando em `http://localhost:8081`. Nesse modo, o frontend envia as chamadas de API para `http://localhost:8081/api`. Se aparecer erro `405 Method Not Allowed` em `:5500/api/...`, a pagina esta tentando chamar o Live Server em vez do Javalin.

## Configuracao

O arquivo `config.env` fica na pasta `Ars-Promptum` e deve conter os dados locais do banco, SMTP e sessao:

```text
DB_URL=jdbc:mysql://localhost:3306/ars_database?useSSL=false&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8
DB_USER=root
DB_PASS=

SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seu-email@gmail.com
SMTP_PASS=sua-senha-de-app

BASE_URL=http://localhost:8081/pages

SESSION_SECRET=troque-por-uma-chave-local-grande-e-aleatoria
SESSION_TTL_HOURS=8
```

`SESSION_SECRET` assina os tokens de login usados pelas rotas administrativas. Use um valor longo e exclusivo no seu ambiente local.
