# Requisitos do Sistema - Ars Promptum

Este documento descreve os requisitos alinhados ao estado atual do codigo. Recursos planejados ficam marcados como pendentes para evitar confusao no GitHub entre o que ja existe e o que ainda sera desenvolvido.

## Visao Geral

Ars Promptum e uma plataforma local para usuarios criarem, organizarem e gerenciarem prompts de IA por categoria, com autenticacao, verificacao de e-mail e painel administrativo.

## Requisitos Funcionais Implementados

### RF-01: Cadastro de Usuario

- O usuario pode criar uma conta com nome, e-mail e senha.
- O sistema valida regras basicas de e-mail e senha.
- O sistema envia um link de verificacao para o e-mail informado.
- O usuario so pode fazer login apos verificar o e-mail.

### RF-02: Login e Sessao

- O usuario pode fazer login com e-mail e senha.
- O frontend salva os dados basicos da sessao em `sessionStorage`.
- A pagina de usuario valida se existe um `usuario_id` numerico antes de carregar prompts.
- Administradores possuem login separado pelo mesmo endpoint, com tipo `admin`.

### RF-03: Recuperacao de Senha

- O usuario pode solicitar redefinicao de senha por e-mail.
- O sistema gera token temporario de redefinicao.
- O usuario pode cadastrar nova senha usando o token recebido.

### RF-04: CRUD de Prompts

- O usuario pode criar prompts com titulo, conteudo, descricao opcional e categoria.
- O usuario pode listar seus prompts.
- O usuario pode visualizar um prompt pelo ID.
- O usuario pode editar titulo, conteudo, descricao e categoria.
- O usuario pode excluir prompts.
- O sistema registra datas de criacao e atualizacao no banco.

Observacao: a exclusao atual remove o registro diretamente. A lixeira com `deletedAt` esta planejada.

### RF-05: Categorias

- O sistema possui tabela de categorias no banco.
- A pagina de usuario carrega as categorias existentes do banco.
- O usuario seleciona uma categoria cadastrada ao criar ou editar prompt.
- O administrador pode criar, editar e excluir categorias.

### RF-06: Painel Administrativo

- O administrador pode visualizar estatisticas gerais.
- O administrador pode listar usuarios.
- O administrador pode promover usuario para administrador.
- O administrador pode deletar usuarios.
- O administrador pode listar, editar e deletar prompts.
- O administrador pode gerenciar categorias.
- O administrador pode consultar logs do sistema.

### RF-07: Logs

- O sistema registra acoes importantes de usuarios e administradores.
- Os logs incluem a acao executada, detalhes e data.
- O painel administrativo permite consulta dos logs.

### RF-08: Configuracao Segura

- Credenciais de banco e SMTP nao ficam fixas no codigo.
- O sistema le configuracoes de variaveis de ambiente ou `config.env`.
- O arquivo `config.env` e ignorado pelo Git.
- O arquivo `config.env.example` serve como modelo publico.

## Requisitos Planejados

### RF-P01: Favoritos

Status: pendente.

- O usuario podera favoritar e desfavoritar prompts.
- Os favoritos deverao ser persistidos por usuario.
- A interface devera oferecer uma visao ou filtro para prompts favoritos.

### RF-P02: Lixeira com `deletedAt`

Status: pendente.

- A exclusao de prompts devera virar soft delete.
- A tabela `prompts` devera receber uma coluna `deleted_at` ou equivalente.
- Prompts excluidos deverao ficar recuperaveis por um periodo definido.
- Uma rotina futura podera remover definitivamente prompts antigos da lixeira.

## Requisitos Fora do Escopo Atual

Os itens abaixo nao fazem parte da meta atual do codigo:

- Tags multiplas por prompt.
- Busca semantica.
- Compartilhamento publico por link.
- Controle de visibilidade privado/time/publico.
- Contador de uso.
- Modal de preenchimento automatico de variaveis `{{variavel}}`.
