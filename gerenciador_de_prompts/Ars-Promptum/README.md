# Como Rodar o Ars Promptum

Este projeto roda pelo Maven. O fluxo principal usa o Maven Wrapper do backend, sem depender de uma instalacao global do Maven.

## Requisitos

- JDK instalado e `JAVA_HOME` configurado.
- MySQL/XAMPP iniciado.
- Arquivo `config.env` configurado na pasta `Ars-Promptum`.
- Banco criado com `BackEnd/database/ars_database_v2_1.sql`.

O backend sobe em:

```text
http://localhost:8081
```

## Rodar o Backend com Maven Wrapper

No PowerShell, entre na pasta do backend:

```powershell
cd C:\xampp\tomcat\webapps\Prompt-Manager-main\gerenciador_de_prompts\Ars-Promptum\BackEnd
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
cd C:\xampp\tomcat\webapps\Prompt-Manager-main\gerenciador_de_prompts\Ars-Promptum\BackEnd
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
cd C:\xampp\tomcat\webapps\Prompt-Manager-main\gerenciador_de_prompts\Ars-Promptum\BackEnd
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

```powershell
cd C:\xampp\tomcat\webapps\Prompt-Manager-main\gerenciador_de_prompts
mvn clean package
```

Para executar apenas o modulo do backend:

```powershell
cd C:\xampp\tomcat\webapps\Prompt-Manager-main\gerenciador_de_prompts
mvn -pl Ars-Promptum/BackEnd clean package
```

## Abrir o Frontend

Com o backend rodando, abra no navegador:

```text
C:\xampp\tomcat\webapps\Prompt-Manager-main\gerenciador_de_prompts\Ars-Promptum\FrontEnd\pages\index.html
```
