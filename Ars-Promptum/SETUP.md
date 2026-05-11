# Ars Prompt — Guia de Configuração (VS Code)

## Estrutura final do projeto

```
BackEnd/
├── lib/
│   └── mysql-connector-j-9.x.x.jar   ← você coloca aqui
├── out/                               ← gerado na compilação
└── src/
    ├── CRUD/
    │   ├── App.java                   ← servidor HTTP principal
    │   ├── DatabaseConnection.java    ← conexão JDBC
    │   ├── dao/
    │   │   ├── UsuarioDAO.java
    │   │   ├── PromptDAO.java
    │   │   ├── CategoriaDAO.java
    │   │   └── LogDAO.java
    │   └── model/
    │       ├── Usuario.java
    │       ├── Prompt.java
    │       └── Categoria.java
    └── Home Page/
        ├── index.html
        ├── View.html
        ├── cadastro.html
        ├── admin.html
        └── css/
            ├── style.css
            └── admin.css
```

---

## Passo 1 — Baixar o MySQL Connector

1. Acesse: https://dev.mysql.com/downloads/connector/j/
2. Em "Select Operating System" escolha **Platform Independent**
3. Baixe o arquivo **.zip**
4. Extraia o zip — dentro haverá um arquivo chamado:
   `mysql-connector-j-9.x.x.jar`
5. Copie esse `.jar` para a pasta `BackEnd/lib/`

---

## Passo 2 — Instalar extensões no VS Code

Abra o VS Code e instale:

- **Extension Pack for Java** (Microsoft)
  → busque por "Extension Pack for Java" na aba Extensions (Ctrl+Shift+X)

Essa extensão instala tudo que é necessário para compilar e rodar Java.

---

## Passo 3 — Adicionar o JAR ao projeto

No VS Code, com o projeto aberto:

1. Pressione `Ctrl+Shift+P` → digite **"Java: Configure Classpath"**
2. Clique em **"Referenced Libraries"**
3. Clique no **+** e selecione o arquivo `lib/mysql-connector-j-9.x.x.jar`

O VS Code vai reconhecer o driver automaticamente.

---

## Passo 4 — Iniciar o XAMPP

1. Abra o XAMPP Control Panel
2. Clique em **Start** no módulo **MySQL**
3. Confirme que está rodando (fica verde)
4. Acesse http://localhost/phpmyadmin para verificar

---

## Passo 5 — Criar o banco

1. Abra o **MySQL Workbench**
2. Conecte em `localhost` com usuário `root` (sem senha)
3. Vá em File → Open SQL Script → abra o arquivo `ars_database_v2.sql`
4. Execute tudo com `Ctrl+Shift+Enter` (raio ⚡)
5. Confirme que as tabelas foram criadas

---

## Passo 6 — Compilar

Abra o terminal no VS Code (`Ctrl+` `) dentro da pasta `BackEnd/`:

### Windows (PowerShell ou cmd):
```
mkdir out
javac -cp "lib/*" -d out src/CRUD/model/*.java src/CRUD/dao/*.java src/CRUD/DatabaseConnection.java src/CRUD/App.java
```

> Se aparecer erro de "EmailException not found", adicione também os arquivos de exceção:
> `src/CRUD/EmailException.java src/CRUD/SenhaException.java`

---

## Passo 7 — Rodar o servidor

### Windows:
```
java -cp "out;lib/*" App
```

### Mac/Linux:
```
java -cp "out:lib/*" App
```

Você verá no terminal:
```
[DB] Banco conectado com sucesso!
========================================
  Ars Prompt — Servidor rodando!
  http://localhost:8081
========================================
```

---

## Passo 8 — Abrir o front-end

Abra o arquivo `src/Home Page/index.html` no navegador.

> Dica: com a extensão **Live Server** do VS Code, clique com botão direito no
> arquivo HTML → "Open with Live Server" para ter reload automático.

---

## Testando os endpoints

Com o servidor rodando, você pode testar pelo navegador ou terminal:

```bash
# Cadastrar usuário
curl -X POST http://localhost:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"username":"wallyson","email":"wallyson@email.com","password":"senha1234"}'

# Listar categorias
curl http://localhost:8081/api/categorias

# Login
curl -X POST http://localhost:8081/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"wallyson@email.com","password":"senha1234"}'
```

---

## Credenciais do admin padrão

- **E-mail:** admin@arsprompt.local
- **Senha:** admin123

> Troque a senha assim que o sistema estiver funcionando!

---

## Problemas comuns

| Erro | Solução |
|------|---------|
| `Driver MySQL não encontrado` | Verifique se o `.jar` está em `lib/` e no classpath |
| `Connection refused` | O MySQL do XAMPP não está rodando |
| `Unknown database 'ars_database'` | Execute o script SQL no Workbench |
| `CORS error` no navegador | Certifique-se de estar rodando o HTML via servidor (Live Server) |
| `EmailException not found` | Compile os arquivos de exceção junto |
