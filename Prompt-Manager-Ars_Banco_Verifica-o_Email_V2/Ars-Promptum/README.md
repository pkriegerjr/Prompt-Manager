# Prompt-Manager: Ars Promptum

Aplicacao local em Java para cadastro, login, verificacao de e-mail e gerenciamento de prompts por usuario, com painel administrativo para usuarios, prompts, categorias e logs.

## Estrutura Atual

=====================text==========================
BackEnd/
  src/main/           Codigo Java do servidor HTTP
  database/           Schema atual e migracoes
  libs/               Dependencias JDBC e JavaMail
  scripts/            Scripts para compilar e executar

FrontEnd/
  pages/              Telas HTML
  assets/css/         Estilos das telas

config.env.example    Modelo seguro de configuracao local
config.env            Configuracao local real, ignorada pelo Git
=================================================================

## Configuracao Local

1. Copie `config.env.example` para `config.env`.
2. Preencha `SMTP_USER`, `SMTP_PASS`, `DB_URL`, `DB_USER`, `DB_PASS` e `BASE_URL`.
3. Nunca publique `config.env`: ele contem credenciais locais e esta no `.gitignore`.

O `App.java` procura as configuracoes em variaveis de ambiente primeiro e depois em `config.env`.

## Como Rodar

1. Inicie o MySQL pelo XAMPP.
2. Para banco novo, execute `BackEnd/database/ars_database_v2_1.sql`.
3. Para banco existente criado antes desta reorganizacao, execute tambem `BackEnd/database/migrations/001_add_prompt_descricao.sql`.
4. Rode `BackEnd/scripts/rodar.bat` no Windows ou `BackEnd/scripts/rodar.sh` no Linux/macOS.
5. Abra `FrontEnd/pages/index.html` no navegador.

O backend sobe em `http://localhost:8081`.

## Observacoes Tecnicas

- O projeto ainda concentra muitas responsabilidades em `App.java`.
- A proxima etapa recomendada e separar o backend em camadas `model`, `repository/dao`, `service`, `controller`, `routes`, `config` e `util`.
- Credenciais de banco e SMTP agora devem ficar em `config.env` ou variaveis de ambiente, nunca no codigo.
