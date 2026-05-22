# Ars Promptum

Ars Promptum e uma aplicacao local para cadastro, login, verificacao de e-mail e gerenciamento de prompts por usuario, com painel administrativo para usuarios, prompts, categorias e logs.

A versao atual usa Maven como fluxo principal do backend. O projeto possui um `pom.xml` pai na raiz `gerenciador_de_prompts` e um modulo Maven executavel em `Ars-Promptum/BackEnd`.

## Status Atual

Implementado:

- Cadastro e login de usuarios.
- Verificacao de e-mail por token.
- Recuperacao/redefinicao de senha.
- Login separado para administradores.
- CRUD de prompts por usuario.
- Categorias carregadas do banco de dados.
- Gerenciamento administrativo de usuarios, prompts e categorias.
- Historico de logs para acoes principais.
- Separacao em `controller`, `dao`, `model`, `service`, `config` e `util`.
- Backend HTTP com Javalin na porta `8081`.
- Configuracao sensivel via `config.env` ou variaveis de ambiente.
- Maven Wrapper no backend.
- JAR executavel gerado pelo Maven Shade Plugin.
- Projeto Maven pai na raiz para agregar modulos.
- Testes automatizados com JUnit 5 para smoke test HTTP do Javalin e tokens de sessao.

Em aberto:

- Favoritos por usuario.
- Lixeira de prompts com `deletedAt` e exclusao definitiva posterior.

Fora do escopo da versao atual:

- Tags multiplas.
- Busca semantica.
- Compartilhamento publico por link.
- Visibilidade por nivel (`PRIVATE`, `TEAM`, `PUBLIC`).

## Estrutura

```text
Prompt-Manager/
  .gitignore                      Regras para ignorar configs locais e builds
  AGENTS.md                       Atores humanos e agentes de sistema
  README.md                       Documentacao principal
  requirements.md                 Requisitos funcionais atuais

  gerenciador_de_prompts/
    pom.xml                       Projeto Maven pai

    Ars-Promptum/
      README.md                   Guia direto de execucao
      config.env.example          Modelo de configuracao local
      config.env                  Configuracao real local, ignorada pelo Git

      BackEnd/
        pom.xml                   Build Maven do backend
        mvnw, mvnw.cmd            Maven Wrapper
        .mvn/wrapper/             Configuracao do Maven Wrapper
        src/main/java/
          App.java                Entrada da aplicacao e registro das rotas HTTP
          config/                 Leitura de variaveis de ambiente e config.env
          controller/             Handlers HTTP por area funcional
          dao/                    Acesso ao banco de dados
          model/                  Entidades do dominio
          service/                Servicos de e-mail e paginas auxiliares
          util/                   Utilitarios HTTP, seguranca e sessao
        src/test/java/            Testes automatizados JUnit
        database/                 Schema SQL e migracoes
        scripts/                  Scripts que usam o Maven Wrapper

      FrontEnd/
        pages/                    Telas HTML
        assets/css/               Estilos das telas
```

## Requisitos

- JDK 17 ou superior instalado e `JAVA_HOME` configurado.
- MySQL iniciado (via XAMPP ou instalacao local).
- Banco criado com `Ars-Promptum/BackEnd/database/ars_database_v2_2.sql`.
- Arquivo `Ars-Promptum/config.env` criado a partir de `Ars-Promptum/config.env.example`.

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

## Como Rodar

### Metodo Recomendado: Maven Wrapper

No Windows PowerShell:

```powershell
cd C:\Users\SONY VAIO\OneDrive\Documentos\vscode\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
.\mvnw.cmd clean compile exec:java
```

No Linux/macOS:

```bash
cd /caminho/do/projeto/gerenciador_de_prompts/Ars-Promptum/BackEnd
./mvnw clean compile exec:java
```

Esse metodo usa o Maven Wrapper versionado no projeto e nao depende de Maven instalado globalmente.

### Rodar pelo Script

No Windows PowerShell:

```powershell
cd C:\Users\SONY VAIO\OneDrive\Documentos\vscode\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
.\scripts\rodar.bat
```

No Linux/macOS:

```bash
cd /caminho/do/projeto/gerenciador_de_prompts/Ars-Promptum/BackEnd
./scripts/rodar.sh
```

Os scripts tambem usam o Maven Wrapper.

### Gerar e Rodar o JAR Executavel

No Windows PowerShell:

```powershell
cd C:\Users\SONY VAIO\OneDrive\Documentos\vscode\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
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

### Usar o Maven Pai

Na raiz `gerenciador_de_prompts`, existe um `pom.xml` agregador. Com Maven instalado no `PATH`, rode:

```powershell
cd C:\Users\SONY VAIO\OneDrive\Documentos\vscode\Prompt-Manager\gerenciador_de_prompts
mvn clean package
```

Para empacotar apenas o modulo do backend:

```powershell
cd C:\Users\SONY VAIO\OneDrive\Documentos\vscode\Prompt-Manager\gerenciador_de_prompts
mvn -pl Ars-Promptum/BackEnd clean package
```

## Como Rodar os Testes

Os testes ficam em `gerenciador_de_prompts/Ars-Promptum/BackEnd/src/test/java`.

- `AppSmokeTest`: sobe o Javalin em uma porta dinamica e valida rotas HTTP, frontend estatico, handlers globais e autorizacao por token.
- `SessionTokenTest`: valida emissao, leitura e rejeicao de token malformado/adulterado.

No Windows PowerShell, usando o Maven Wrapper:

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

O arquivo `Ars-Promptum/config.env` deve conter valores reais de ambiente local:

```text
DB_URL=jdbc:mysql://localhost:3306/ars_database
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

Nunca publique `config.env`. Use apenas `config.env.example` como modelo seguro no GitHub.

`SESSION_SECRET` assina os tokens de login usados pelas rotas administrativas. Use um valor longo e exclusivo no seu ambiente local.

## Rotas Principais

```text
POST /api/usuarios
GET  /api/verificar
POST /api/reenviar
POST /api/esqueci-senha
POST /api/redefinir-senha
POST /api/login

GET  /api/prompts?uid={usuarioId}
GET  /api/prompts/{id}
POST /api/prompts
PUT  /api/prompts/{id}
DELETE /api/prompts/{id}

GET  /api/categorias

GET  /api/admin/stats
GET  /api/admin/usuarios
POST /api/admin/usuarios/{id}/ativar
POST /api/admin/usuarios/{id}/desativar
POST /api/admin/tornar-admin
POST /api/admin/revogar-admin
DELETE /api/admin/deletar-usuario/{id}
GET  /api/admin/prompts
PUT  /api/admin/prompts/{id}
DELETE /api/admin/prompts/{id}
GET  /api/admin/categorias
POST /api/admin/categorias
PUT  /api/admin/categorias/{id}
DELETE /api/admin/categorias/{id}
GET  /api/admin/logs
POST /api/admin/criar-admin
```

## Observacoes de Seguranca

- Credenciais ficam fora do codigo fonte.
- `Ars-Promptum/config.env` deve permanecer no `.gitignore`.
- Senhas sao armazenadas com SHA-256 no estado atual do projeto.
- Para uso real em producao, o ideal e migrar o armazenamento de senhas para BCrypt.

## Proximos Passos Planejados

1. Implementar favoritos por usuario.
2. Implementar lixeira usando `deletedAt`.
3. Ajustar `DELETE /api/prompts/{id}` para soft delete.
4. Criar rotina para remocao definitiva de prompts antigos na lixeira.
5. Evoluir a camada de servico para concentrar regras de negocio fora dos controllers.
