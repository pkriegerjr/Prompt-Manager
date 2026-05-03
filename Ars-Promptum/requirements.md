# Requisitos do Sistema - Plataforma de Prompts

## Visão Geral
Plataforma para devs e copywriters salvarem, categorizarem e compartilharem seus melhores prompts de IA.

## Requisitos Funcionais (RF)

### RF-01: CRUD de Prompts
- **Criar:** Título, corpo (suporta `{{variavel}}`), descrição opcional e categoria.
- **Editar:** Qualquer campo + registro de data de modificação e autor.
- **Excluir:** Requer confirmação; lixeira de 30 dias.
- **Visualizar:** Detalhes completos, metadados, tags e contador de uso.

### RF-02: Tags e Categorização
- **Associar Tags:** Múltiplas tags por prompt com autocomplete.
- **Gerenciar Tags:** CRUD de tags; desassociação automática ao excluir tag.
- **Filtrar:** Filtros múltiplos com lógica AND.

### RF-03: Busca Semântica
- Busca por intenção (linguagem natural).
- Ranking por relevância, data ou popularidade.
- Integração com filtros existentes.

### RF-04: Cópia Inteligente
- Botão de cópia direta.
- Modal de preenchimento de variáveis para `{{variavel}}`.
- Feedback visual e incremento de contador de uso.

### RF-05: Favoritos
- Sistema de toggle (favoritar/desfavoritar).
- Persistência por usuário.
- Visão dedicada para favoritos.

### RF-06: Compartilhamento
- Geração de links públicos (somente leitura) revogáveis.
- Níveis de visibilidade: Privado, Time ou Público.
