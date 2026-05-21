# Como Rodar o Ars Promptum

Este projeto roda pelo Maven. O fluxo principal usa o Maven Wrapper do backend, sem depender de uma instalacao global do Maven.

## Requisitos

- JDK 17 ou superior instalado e `JAVA_HOME` configurado.
- MySQL/XAMPP iniciado.
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
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
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
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
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
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
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
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts
mvn clean package
```

Para executar apenas o modulo do backend:

```powershell
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts
mvn -pl Ars-Promptum/BackEnd clean package
```

## Abrir o Frontend

Com o backend rodando, abra no navegador:

```text
http://localhost:8081/
```

O Javalin redireciona a raiz para `http://localhost:8081/pages/index.html` e serve os assets de `FrontEnd/assets`.
