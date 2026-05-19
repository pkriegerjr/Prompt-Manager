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
- Maven Wrapper no backend, com suporte a execucao direta e JAR executavel.
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
BackEnd/
  pom.xml            Build Maven do backend
  mvnw, mvnw.cmd     Maven Wrapper
  .mvn/wrapper/      Configuracao do Maven Wrapper
  src/main/java/
    App.java          Entrada da aplicacao e registro das rotas HTTP
    config/           Leitura de variaveis de ambiente e config.env
    controller/       Handlers HTTP por area funcional
    dao/              Acesso ao banco de dados
    model/            Entidades do dominio
    service/          Servicos de e-mail e paginas auxiliares
    util/             Utilitarios HTTP, JSON e seguranca
  database/           Schema SQL e migracoes
  scripts/            Scripts para compilar e executar

FrontEnd/
  pages/              Telas HTML
  assets/css/         Estilos das telas

pom.xml               Projeto Maven pai
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

### Rodando com Maven Wrapper

A migracao para Maven ja usa o layout padrao `BackEnd/src/main/java`. O caminho recomendado e usar o Maven Wrapper versionado no backend:

```text
cd Ars-Promptum/BackEnd
mvnw.cmd clean compile exec:java
```

No Linux/macOS:

```text
cd Ars-Promptum/BackEnd
./mvnw clean compile exec:java
```

O Maven Wrapper precisa que `JAVA_HOME` esteja configurado para o JDK instalado.

Ou usar os scripts:

```text
BackEnd/scripts/rodar.bat
BackEnd/scripts/rodar.sh
```

Esses scripts tambem usam o Maven Wrapper.

Tambem e possivel rodar comandos Maven a partir da raiz do projeto:

```text
mvn -pl Ars-Promptum/BackEnd clean package
```

Ou todos os modulos Maven:

```text
mvn clean package
```

### Gerando e Rodando o JAR

O backend tambem pode ser empacotado como JAR executavel:

```text
cd Ars-Promptum/BackEnd
mvnw.cmd clean package
java -jar target/ars-promptum-backend-1.0.0-SNAPSHOT.jar
```

No Linux/macOS:

```text
cd Ars-Promptum/BackEnd
./mvnw clean package
java -jar target/ars-promptum-backend-1.0.0-SNAPSHOT.jar
```

Execute o JAR a partir da pasta `BackEnd`, para que o `config.env` em `Ars-Promptum/config.env` seja encontrado corretamente.

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
