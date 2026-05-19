# Ars Promptum

Ars Promptum e uma aplicacao local para cadastro, login, verificacao de e-mail e gerenciamento de prompts por usuario, com painel administrativo para usuarios, prompts, categorias e logs.

O projeto esta em fase de organizacao para boas praticas de Programacao Orientada a Objetos. A versao atual prioriza uma base funcional, segura e simples de executar em ambiente local com Java, MySQL/XAMPP e frontend em HTML, CSS e JavaScript.

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
- Separacao inicial em `controller`, `dao`, `model`, `service`, `config` e `util`.
- Configuracao sensivel via `config.env` ou variaveis de ambiente.

Em aberto:

- Favoritos por usuario.
- Lixeira de prompts com `deletedAt` e exclusao definitiva posterior.

Fora do escopo da versao atual:

- Tags multiplas.
- Busca semantica.
- Compartilhamento publico por link.
- Visibilidade por nivel (`PRIVATE`, `TEAM`, `PUBLIC`).
- Maven ou Gradle.

## Estrutura

```text
BackEnd/
  src/main/
    App.java          Entrada da aplicacao e registro das rotas HTTP
    config/           Leitura de variaveis de ambiente e config.env
    controller/       Handlers HTTP por area funcional
    dao/              Acesso ao banco de dados
    model/            Entidades do dominio
    service/          Servicos de e-mail e paginas auxiliares
    util/             Utilitarios HTTP, JSON e seguranca
  database/           Schema SQL e migracoes
  libs/               Dependencias JDBC e JavaMail
  scripts/            Scripts para compilar e executar

FrontEnd/
  pages/              Telas HTML
  assets/css/         Estilos das telas

config.env.example    Modelo de configuracao local
config.env            Configuracao real local, ignorada pelo Git
requirements.md       Requisitos funcionais atuais
SPECS.md              Especificacoes tecnicas atuais
```

## Como Rodar

1. Inicie o MySQL pelo XAMPP.
2. Execute `BackEnd/database/ars_database_v2_1.sql` para criar o banco.
3. Copie `config.env.example` para `config.env`.
4. Preencha as variaveis de banco, SMTP e `BASE_URL`.
5. Execute `BackEnd/scripts/rodar.bat` no Windows.
6. Abra `FrontEnd/pages/index.html` no navegador.

O backend sobe em:

```text
http://localhost:8081
```

## Configuracao

O arquivo `config.env` deve conter valores reais de ambiente local:

```text
DB_URL=jdbc:mysql://localhost:3306/ars_database
DB_USER=root
DB_PASS=

SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seu-email@gmail.com
SMTP_PASS=sua-senha-de-app

BASE_URL=http://localhost:8080/Ars-Promptum/FrontEnd/pages
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
- `config.env` deve permanecer no `.gitignore`.
- Senhas sao armazenadas com SHA-256 no estado atual do projeto.
- Para uso real em producao, o ideal e migrar o armazenamento de senhas para BCrypt.

## Proximos Passos Planejados

1. Implementar favoritos por usuario.
2. Implementar lixeira usando `deletedAt`.
3. Ajustar `DELETE /api/prompts/{id}` para soft delete.
4. Criar rotina para remocao definitiva de prompts antigos na lixeira.
5. Evoluir a camada de servico para concentrar regras de negocio fora dos controllers.
