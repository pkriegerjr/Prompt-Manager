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
          util/                   Utilitarios HTTP, JSON e seguranca
        database/                 Schema SQL e migracoes
        scripts/                  Scripts que usam o Maven Wrapper

      FrontEnd/
        pages/                    Telas HTML
        assets/css/               Estilos das telas
```

## Requisitos

- JDK 17 ou superior instalado e `JAVA_HOME` configurado.
- MySQL/XAMPP iniciado.
- Banco criado com `Ars-Promptum/BackEnd/database/ars_database_v2_2.sql`.
- Arquivo `Ars-Promptum/config.env` criado a partir de `Ars-Promptum/config.env.example`.

O backend sobe em:

```text
http://localhost:8081
```

## Como Rodar

### Metodo Recomendado: Maven Wrapper

No Windows PowerShell:

```powershell
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
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
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts\Ars-Promptum\BackEnd
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

### Usar o Maven Pai

Na raiz `gerenciador_de_prompts`, existe um `pom.xml` agregador. Com Maven instalado no `PATH`, rode:

```powershell
cd C:\xampp\tomcat\webapps\Prompt-Manager\gerenciador_de_prompts
mvn clean package
```

Para empacotar apenas o modulo do backend:

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
```

Nunca publique `config.env`. Use apenas `config.env.example` como modelo seguro no GitHub.

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
