# Especificações Técnicas (SPECS) - Prompt Manager

## 1. Arquitetura de Dados (POO)

### Classe `Prompt`
Atributos principais para suportar os RFs:
- `id: UUID`
- `title: String`
- `body: String` (Processamento de Regex para identificar `{{variavel}}`)
- `description: String`
- `category: Category` (Enum ou Objeto)
- `tags: List<Tag>`
- `author: User`
- `usageCount: int`
- `createdAt / updatedAt: LocalDateTime`
- `visibility: Visibility` (Enum: PRIVATE, TEAM, PUBLIC)
- `deletedAt: LocalDateTime` (Para lógica de lixeira de 30 dias)

### Classe `Tag`
- `id: Long`
- `name: String` (Único)

## 2. Lógicas de Negócio Críticas

### Processamento de Variáveis (RF-04)
O sistema deve implementar uma lógica de *Parsing*. 
- **Método:** `extractVariables(String body)`
- **Ação:** Identifica padrões dentro de chaves duplas e retorna uma lista de strings para o Modal de preenchimento.

### Busca e Ranking (RF-03)
Como o projeto é em Java:
- **Simulação Semântica:** Em um cenário acadêmico, pode-se usar uma interface `SearchStrategy` para alternar entre busca por String simples e uma integração com IA (ex: LangChain4j).

### Sistema de Lixeira (RF-01)
- **Soft Delete:** O método `delete()` não remove do banco, apenas seta `deletedAt`.
- **Scheduled Task:** Um serviço que roda periodicamente para remover permanentemente itens onde `deletedAt` > 30 dias.

## 3. Interfaces de Serviço
- `PromptService`: Interface com métodos CRUD e regras de validação.
- `Shareable`: Interface para objetos que podem gerar links públicos.

## 4. Stack Técnica Definitiva
- **Linguagem:** Java 17+[cite: 1]
- **Paradigma:** Orientação a Objetos (POO)
- **Gerenciador de Dependências:** Maven ou Gradle
