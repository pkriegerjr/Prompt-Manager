# Especificacoes Tecnicas - Ars Promptum

Este documento descreve a arquitetura tecnica atual do projeto e os pontos planejados para evolucao.

## Stack Atual

- Linguagem backend: Java.
- Servidor HTTP: `com.sun.net.httpserver.HttpServer`.
- Banco de dados: MySQL via XAMPP.
- Driver: MySQL Connector/J em `BackEnd/libs`.
- E-mail: JavaMail em `BackEnd/libs`.
- Frontend: HTML, CSS, JavaScript e Bootstrap.
- Build atual: scripts com `javac`.
- Gerenciador de dependencias: ainda nao usa Maven ou Gradle.

## Organizacao Backend

```text
BackEnd/src/main/
  App.java
  config/
  controller/
  dao/
  model/
  service/
  util/
```

### `App.java`

Responsavel por:

- Abrir conexao inicial com o banco.
- Executar migracoes simples de compatibilidade.
- Registrar rotas HTTP.
- Iniciar o servidor na porta `8081`.

O objetivo e manter `App.java` apenas como ponto de entrada e roteamento.

### `controller`

Camada responsavel por receber requisicoes HTTP, validar entrada basica e chamar DAOs/servicos.

Controllers atuais:

- `AuthController`
- `PromptController`
- `CategoriaController`
- `AdminController`

### `dao`

Camada responsavel por acesso direto ao banco via JDBC.

DAOs atuais:

- `Database`
- `UsuarioDao`
- `AdminDao`
- `PromptDao`
- `CategoriaDao`
- `LogDao`
- `AdminStatsDao`

### `model`

Entidades simples usadas para transportar dados do banco para os controllers.

Models atuais:

- `Usuario`
- `Admin`
- `Prompt`
- `Categoria`
- `LogEntry`
- `AdminStats`

### `service`

Servicos de apoio.

Servicos atuais:

- `EmailService`
- `VerificationPage`

### `util`

Utilitarios compartilhados.

Utilitarios atuais:

- `HttpUtil`
- `JsonUtil`
- `JsonViews`
- `RouteHandler`
- `SecurityUtil`

## Modelo de Dados Atual

### `usuarios`

Campos principais:

- `id`
- `username`
- `email`
- `password`
- `ativo`
- `verificado`
- `token_verificacao`
- `token_expira`
- `role`
- `criado_em`

### `admins`

Campos principais:

- `id`
- `verificado`
- `token_verificacao`
- `token_expira`
- `username`
- `email`
- `password`
- `criado_em`

### `categorias`

Campos principais:

- `id`
- `nome`
- `descricao`

### `prompts`

Campos principais:

- `id`
- `usuario_id`
- `categoria_id`
- `titulo`
- `conteudo`
- `descricao`
- `criado_em`
- `atualizado_em`

Planejado:

- `deleted_at` ou `deletedAt` para lixeira/soft delete.

### `historico_logs`

Campos principais:

- `id`
- `usuario_id`
- `admin_id`
- `acao`
- `detalhes`
- `feito_em`

## Rotas Backend

### Autenticacao

```text
POST /api/usuarios
GET  /api/verificar
POST /api/reenviar
POST /api/esqueci-senha
POST /api/redefinir-senha
POST /api/login
```

### Prompts

```text
GET    /api/prompts?uid={usuarioId}
GET    /api/prompts/{id}
POST   /api/prompts
PUT    /api/prompts/{id}
DELETE /api/prompts/{id}
```

### Categorias

```text
GET /api/categorias
```

### Admin

```text
GET    /api/admin/stats
GET    /api/admin/usuarios
POST   /api/admin/usuarios/{id}/ativar
POST   /api/admin/usuarios/{id}/desativar
POST   /api/admin/tornar-admin
DELETE /api/admin/deletar-usuario/{id}
GET    /api/admin/prompts
PUT    /api/admin/prompts/{id}
DELETE /api/admin/prompts/{id}
GET    /api/admin/categorias
POST   /api/admin/categorias
PUT    /api/admin/categorias/{id}
DELETE /api/admin/categorias/{id}
GET    /api/admin/logs
POST   /api/admin/criar-admin
```

## Configuracao

A configuracao e lida por `AppConfig`.

Ordem de prioridade:

1. Variaveis de ambiente.
2. `config.env` no diretorio atual ou em diretorios superiores previstos pelo codigo.

Variaveis esperadas:

```text
DB_URL
DB_USER
DB_PASS
SMTP_HOST
SMTP_PORT
SMTP_USER
SMTP_PASS
BASE_URL
```

## Seguranca

Estado atual:

- Credenciais foram removidas do codigo.
- `config.env` deve ser ignorado pelo Git.
- `config.env.example` deve ser publicado como modelo.
- Senhas sao comparadas usando SHA-256.

Melhoria futura recomendada:

- Migrar senhas para BCrypt.

## Evolucoes Planejadas

### Favoritos

Proposta tecnica inicial:

- Criar tabela `favoritos`.
- Relacionar `usuario_id` e `prompt_id`.
- Criar `FavoritoDao`.
- Criar endpoints para favoritar, desfavoritar e listar favoritos.
- Adicionar controle visual na pagina de usuario.

### Lixeira com `deletedAt`

Proposta tecnica inicial:

- Adicionar coluna `deleted_at` em `prompts`.
- Alterar `PromptDao.deletar` para atualizar `deleted_at` em vez de executar `DELETE`.
- Filtrar listagens comuns para ignorar prompts com `deleted_at` preenchido.
- Criar rota administrativa ou de usuario para restaurar prompt.
- Criar rotina futura para exclusao definitiva apos periodo definido.

## Fora do Escopo Atual

Nao fazem parte da arquitetura atual:

- `PromptService`.
- Interface `Shareable`.
- `SearchStrategy`.
- Tags multiplas.
- Busca semantica.
- Links publicos revogaveis.
- Visibilidade `PRIVATE`, `TEAM`, `PUBLIC`.
- ID de prompt como UUID.
