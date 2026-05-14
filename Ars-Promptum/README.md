# Prompt-Manager: Ars Promptum

Aplicacao local em Java para cadastro, login, verificacao de e-mail e gerenciamento de prompts por usuario, com painel administrativo para usuarios, prompts, categorias e logs.

## Estrutura Atual

```text
BackEnd/
  src/main/           Codigo Java do servidor HTTP
  database/           Schema atual, migracoes e SQL legado preservado
  libs/               Dependencias JDBC e JavaMail
  scripts/            Scripts para compilar e executar

FrontEnd/
  pages/              Telas HTML
  assets/css/         Estilos das telas

## Como Rodar

1. Inicie o MySQL pelo XAMPP.
2. Para banco novo, execute `BackEnd/database/ars_database_v2.sql`.
3. Para banco existente criado antes desta reorganizacao, execute tambem `BackEnd/database/migrations/001_add_prompt_descricao.sql`.
4. Rode `BackEnd/scripts/rodar.bat` no Windows ou `BackEnd/scripts/rodar.sh` no Linux/macOS.
  4.1 Ex: No CMD -> cd C:\xampp\tomcat\webapps\Ars-Promptum\BackEnd\scripts && rodar.bat
5. Abra `FrontEnd/pages/index.html` no navegador.

O backend sobe em `http://localhost:8081`.

## Observacoes Tecnicas

- O projeto ainda concentra muitas responsabilidades em `App.java`.
- A proxima etapa recomendada e separar o backend em camadas `model`, `repository/dao`, `service`, `controller`, `routes`, `config` e `util`.
- Credenciais de banco e SMTP devem sair do codigo antes de qualquer publicacao em GitHub.
- Os scripts antigos que apontavam para `src/CRUD` foram preservados em `BackEnd/legacy/scripts`, mas nao sao a forma atual de rodar o projeto.
- A tela antiga de verificacao por codigo foi preservada em `FrontEnd/legacy/verify-codigo.html`; o fluxo atual usa link UUID enviado por email.
