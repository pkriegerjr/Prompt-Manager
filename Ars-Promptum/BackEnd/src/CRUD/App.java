import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import javax.mail.*;
import javax.mail.internet.*;

public class App {

    // ── Banco ────────────────────────────────────────────────
    static final String DB_URL  = "jdbc:mysql://localhost:3306/ars_database?useSSL=false&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";
    static final String DB_USER = "root";
    static final String DB_PASS = "";
    static Connection conn;

    // ── Email SMTP (Gmail) ───────────────────────────────────
    static final String SMTP_HOST = "smtp.gmail.com";
    static final int    SMTP_PORT = 587;
    static final String SMTP_USER = "wallysonsbarbosa@gmail.com";  // ← seu Gmail
    static final String SMTP_PASS = "cxmx kpyw urzm wtcd";  // ← App Password
    static final String BASE_URL    = "http://localhost:8080/Ars-Promptum/BackEnd/src/Home%20Page";

    // ── Conexão banco ────────────────────────────────────────
    static Connection db() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                System.out.println("[DB] Conexao estabelecida.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver nao encontrado. Verifique o jar no -cp");
        }
        return conn;
    }

    // ════════════════════════════════════════════════════════
    //  MAIN
    // ════════════════════════════════════════════════════════
    public static void main(String[] args) throws Exception {
        try {
            db();
            System.out.println("[DB] Banco ars_database conectado!");
            migrarSenhas();
        } catch (SQLException e) {
            System.err.println("[ERRO] Nao foi possivel conectar ao banco: " + e.getMessage());
            System.err.println(">>> Verifique se o MySQL do XAMPP esta rodando.");
            System.exit(1);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        server.createContext("/api/usuarios",              ex -> rotear(ex, App::handleUsuarios));
        server.createContext("/api/verificar",             ex -> rotear(ex, App::handleVerificar));
        server.createContext("/api/reenviar",              ex -> rotear(ex, App::handleReenviar));
        server.createContext("/api/esqueci-senha",         ex -> rotear(ex, App::handleEsqueciSenha));
        server.createContext("/api/redefinir-senha",       ex -> rotear(ex, App::handleRedefinirSenha));
        server.createContext("/api/login",                 ex -> rotear(ex, App::handleLogin));
        server.createContext("/api/prompts",               ex -> rotear(ex, App::handlePrompts));
        server.createContext("/api/categorias",            ex -> rotear(ex, App::handleCategorias));
        server.createContext("/api/admin/stats",           ex -> rotear(ex, App::handleAdminStats));
        server.createContext("/api/admin/usuarios",        ex -> rotear(ex, App::handleAdminUsuarios));
        server.createContext("/api/admin/tornar-admin",    ex -> rotear(ex, App::handleTornarAdmin));
        server.createContext("/api/admin/deletar-usuario", ex -> rotear(ex, App::handleDeletarUsuario));
        server.createContext("/api/admin/prompts",         ex -> rotear(ex, App::handleAdminPrompts));
        server.createContext("/api/admin/categorias",      ex -> rotear(ex, App::handleAdminCategorias));
        server.createContext("/api/admin/logs",            ex -> rotear(ex, App::handleAdminLogs));
        server.createContext("/api/admin/criar-admin",     ex -> rotear(ex, App::handleCriarAdmin));

        System.out.println("========================================");
        System.out.println("  Ars Prompt rodando em localhost:8081");
        System.out.println("========================================");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { if (conn!=null&&!conn.isClosed()) conn.close(); } catch(Exception ignored){}
        }));
    }

    interface Handler { void handle(HttpExchange ex) throws Exception; }

    static void rotear(HttpExchange ex, Handler h) throws IOException {
        cors(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { ex.sendResponseHeaders(204,-1); ex.close(); return; }
        try { h.handle(ex); }
        catch (Exception e) { System.err.println("[ERRO] "+e.getMessage()); respText(ex,500,"Erro interno: "+e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/usuarios — Cadastro + envia link por email
    // ════════════════════════════════════════════════════════
    static void handleUsuarios(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body  = body(ex);
        String user  = jstr(body,"username");
        String email = jstr(body,"email");
        String senha = jstr(body,"password");

        validarEmail(email);
        validarSenha(senha);

        String token = UUID.randomUUID().toString();

        try (PreparedStatement ps = db().prepareStatement(
                "INSERT INTO usuarios (username, email, password, verificado, token_verificacao, token_expira) " +
                "VALUES (?,?,?,0,?,DATE_ADD(NOW(), INTERVAL 24 HOUR))")) {
            ps.setString(1,user); ps.setString(2,email);
            ps.setString(3,sha256(senha)); ps.setString(4,token);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            respText(ex,400,"Email ou nome de usuario ja cadastrado."); return;
        }

        new Thread(() -> enviarLinkVerificacao(email, user, token, false)).start();
        log(0,"CADASTRO","Novo usuario: "+user);
        respText(ex,200,"Conta criada! Enviamos um link de verificacao para "+email);
    }

    // ════════════════════════════════════════════════════════
    //  GET /api/verificar?token=UUID — clique no link do email
    // ════════════════════════════════════════════════════════
    static void handleVerificar(HttpExchange ex) throws Exception {
        String query = ex.getRequestURI().getQuery();
        String token = qstr(query, "token");
        String tipo  = qstr(query, "tipo");

        if (token.isEmpty()) { respHtml(ex, paginaResultado("❌ Link inválido", "Token ausente ou malformado.", false)); return; }

        // Admins não precisam de verificação por email
        boolean isAdmin = "admin".equals(tipo);
        if (isAdmin) { respHtml(ex, paginaResultado("✓ Acesso liberado!", "Administradores não precisam verificar email. Faça login normalmente.", true)); return; }

        try (PreparedStatement ps = db().prepareStatement(
                "SELECT id, token_expira, verificado FROM usuarios WHERE token_verificacao=?")) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                respHtml(ex, paginaResultado("❌ Link inválido", "Este link já foi utilizado ou não existe.", false)); return;
            }
            if (rs.getInt("verificado") == 1) {
                respHtml(ex, paginaResultado("✓ Já verificado!", "Sua conta já estava verificada. Você já pode fazer login.", true)); return;
            }
            Timestamp expira = rs.getTimestamp("token_expira");
            if (expira != null && expira.before(new Timestamp(System.currentTimeMillis()))) {
                respHtml(ex, paginaResultado("⏱ Link expirado", "O link de verificação expirou. Faça login para receber um novo link.", false)); return;
            }

            int id = rs.getInt("id");
            try (PreparedStatement up = db().prepareStatement(
                    "UPDATE usuarios SET verificado=1, ativo=1, token_verificacao=NULL, token_expira=NULL WHERE id=?")) {
                up.setInt(1, id); up.executeUpdate();
            }
            log(id, "EMAIL_VERIFICADO", "via link");
            respHtml(ex, paginaResultado("✓ Email verificado!", "Sua conta está ativa. Clique abaixo para fazer login.", true));
        }
    }

    // ── Página HTML de resultado da verificação ──────────────
    static String paginaResultado(String titulo, String msg, boolean sucesso) {
        String cor    = sucesso ? "#3fb950" : "#f85149";
        String btnUrl = "http://localhost:8080/Ars-Promptum/BackEnd/src/Home%20Page/View.html";
        return "<!DOCTYPE html><html lang='pt-br'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Ars Prompt — Verificação</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;600;700&display=swap' rel='stylesheet'>" +
            "<style>*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:'Space Grotesk',sans-serif;background:#0d1117;color:#e6edf3;" +
            "min-height:100vh;display:flex;align-items:center;justify-content:center;padding:1.5rem}" +
            ".card{background:#161b22;border:1px solid #30363d;border-radius:12px;padding:2.5rem 2rem;" +
            "max-width:420px;width:100%;text-align:center;box-shadow:0 8px 40px rgba(0,0,0,.5)}" +
            ".icon{font-size:3rem;margin-bottom:1rem}" +
            "h1{font-size:1.5rem;font-weight:700;margin-bottom:.75rem;color:"+cor+"}" +
            "p{color:#8b949e;font-size:.9rem;line-height:1.6;margin-bottom:1.75rem}" +
            ".brand{font-family:monospace;font-size:.85rem;color:#58a6ff;margin-bottom:1.5rem}" +
            "a{display:inline-block;background:#58a6ff;color:#0d1117;font-weight:700;" +
            "padding:.75rem 2rem;border-radius:8px;text-decoration:none;font-size:.95rem;transition:background .2s}" +
            "a:hover{background:#79b8ff}</style></head><body>" +
            "<div class='card'><div class='brand'>{ } ars-prompt</div>" +
            "<div class='icon'>"+(sucesso?"🎉":"⚠️")+"</div>" +
            "<h1>"+titulo+"</h1><p>"+msg+"</p>" +
            (sucesso ? "<a href='"+btnUrl+"'>Ir para o login →</a>" : "") +
            "</div></body></html>";
    }

    // ════════════════════════════════════════════════════════
    // ════════════════════════════════════════════════════════
    //  POST /api/reenviar — Reenvia link de verificação
    // ════════════════════════════════════════════════════════
    static void handleReenviar(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String email = jstr(body(ex),"email");

        try (PreparedStatement ps = db().prepareStatement(
                "SELECT username, verificado FROM usuarios WHERE email=?")) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { respText(ex,404,"Email nao encontrado."); return; }
            if (rs.getInt("verificado")==1) { respText(ex,200,"Conta ja verificada! Faca login."); return; }

            String user  = rs.getString("username");
            String token = UUID.randomUUID().toString();
            try (PreparedStatement up = db().prepareStatement(
                    "UPDATE usuarios SET token_verificacao=?, token_expira=DATE_ADD(NOW(), INTERVAL 24 HOUR) WHERE email=?")) {
                up.setString(1,token); up.setString(2,email); up.executeUpdate();
            }
            new Thread(() -> enviarLinkVerificacao(email, user, token, false)).start();
            respText(ex,200,"Novo link enviado para "+email+". Verifique sua caixa de entrada.");
        }
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/esqueci-senha
    //  Body: { "email": "...", "tipo": "usuario"|"admin" }
    // ════════════════════════════════════════════════════════
    static void handleEsqueciSenha(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body  = body(ex);
        String email = jstr(body,"email");
        String tipo  = jor(body,"tipo","usuario");
        String tabela = "admin".equals(tipo) ? "admins" : "usuarios";

        try (PreparedStatement ps = db().prepareStatement(
                "SELECT username FROM "+tabela+" WHERE email=?")) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { respText(ex,404,"Email nao encontrado."); return; }

            String user  = rs.getString("username");
            String token = UUID.randomUUID().toString();

            // Reutiliza coluna token_verificacao para guardar o token de reset
            String sql = "admin".equals(tipo)
                ? "UPDATE admins   SET token_verificacao=?, token_expira=DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email=?"
                : "UPDATE usuarios SET token_verificacao=?, token_expira=DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email=?";

            try (PreparedStatement up = db().prepareStatement(sql)) {
                up.setString(1,token); up.setString(2,email); up.executeUpdate();
            }

            boolean isAdmin = "admin".equals(tipo);
            new Thread(() -> enviarEmailReset(email, user, token, isAdmin)).start();
            respText(ex,200,"Link de redefinicao enviado para "+email+". Verifique sua caixa de entrada.");
        }
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/redefinir-senha
    //  Body: { "email":"...", "token":"...", "novaSenha":"...", "tipo":"..." }
    // ════════════════════════════════════════════════════════
    static void handleRedefinirSenha(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body      = body(ex);
        String email     = jstr(body,"email");
        String token     = jstr(body,"token");
        String novaSenha = jstr(body,"novaSenha");
        String tipo      = jor(body,"tipo","usuario");
        String tabela    = "admin".equals(tipo) ? "admins" : "usuarios";

        if (novaSenha.length() < 9) { respText(ex,400,"Senha deve ter no minimo 9 caracteres."); return; }

        try (PreparedStatement ps = db().prepareStatement(
                "SELECT id, token_verificacao, token_expira FROM "+tabela+" WHERE email=?")) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { respText(ex,404,"Email nao encontrado."); return; }

            String tokenSalvo = rs.getString("token_verificacao");
            if (tokenSalvo == null || !tokenSalvo.equals(token)) { respText(ex,400,"Token invalido."); return; }

            Timestamp expira = rs.getTimestamp("token_expira");
            if (expira != null && expira.before(new Timestamp(System.currentTimeMillis()))) {
                respText(ex,400,"Token expirado. Solicite um novo link."); return;
            }

            try (PreparedStatement up = db().prepareStatement(
                    "UPDATE "+tabela+" SET password=?, token_verificacao=NULL, token_expira=NULL WHERE email=?")) {
                up.setString(1,sha256(novaSenha)); up.setString(2,email); up.executeUpdate();
            }
            respText(ex,200,"Senha redefinida com sucesso! Faca login com a nova senha.");
        }
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/login
    // ════════════════════════════════════════════════════════
    static void handleLogin(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body  = body(ex);
        String email = jstr(body,"email");
        String senha = jstr(body,"password");
        String tipo  = jor(body,"tipo","usuario");

        if ("admin".equals(tipo)) {
            try (PreparedStatement ps = db().prepareStatement("SELECT * FROM admins WHERE email=?")) {
                ps.setString(1,email); ResultSet rs = ps.executeQuery();
                if (!rs.next()) { respText(ex,401,"E-mail ou senha incorretos."); return; }
                if (!rs.getString("password").equals(sha256(senha))) { respText(ex,401,"E-mail ou senha incorretos."); return; }
                respJson(ex,200,String.format("{\"tipo\":\"admin\",\"id\":%d,\"username\":\"%s\"}",
                    rs.getInt("id"),esc(rs.getString("username")))); return;
            }
        } else {
            try (PreparedStatement ps = db().prepareStatement("SELECT * FROM usuarios WHERE email=?")) {
                ps.setString(1,email); ResultSet rs = ps.executeQuery();
                if (!rs.next()) { respText(ex,401,"E-mail ou senha incorretos."); return; }
                if (!rs.getString("password").equals(sha256(senha))) { respText(ex,401,"E-mail ou senha incorretos."); return; }
                if (rs.getInt("verificado")==0) { respText(ex,403,"EMAIL_NAO_VERIFICADO:"+email+":usuario"); return; }
                if (rs.getInt("ativo")==0) { respText(ex,403,"Conta desativada. Contate o administrador."); return; }
                log(rs.getInt("id"),"LOGIN",null);
                respJson(ex,200,String.format("{\"tipo\":\"usuario\",\"id\":%d,\"username\":\"%s\"}",
                    rs.getInt("id"),esc(rs.getString("username")))); return;
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/admin/criar-admin
    // ════════════════════════════════════════════════════════
    static void handleCriarAdmin(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body  = body(ex);
        String user  = jstr(body,"username");
        String email = jstr(body,"email");
        String senha = jstr(body,"password");

        validarEmail(email); validarSenha(senha);

        try (PreparedStatement ps = db().prepareStatement(
                "INSERT INTO admins (username, email, password, verificado) VALUES (?,?,?,1)")) {
            ps.setString(1,user); ps.setString(2,email); ps.setString(3,sha256(senha));
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            respText(ex,400,"Email ou username ja cadastrado."); return;
        }

        logAdmin(1,"ADMIN_CRIADO","Novo admin: "+user);
        respText(ex,200,"Administrador \""+user+"\" criado com sucesso! Ja pode fazer login.");
    }

    // ════════════════════════════════════════════════════════
    //  ENVIO DE EMAIL via Resend API (sem .jar extra)
    // ════════════════════════════════════════════════════════
    static void enviarLinkVerificacao(String destino, String nome, String token, boolean isAdmin) {
        try {
            String link  = "http://localhost:8081/api/verificar?token=" + token
                         + (isAdmin ? "&tipo=admin" : "");
            String papel = isAdmin ? "administrador" : "usuario";

            String html =
                "<div style=\"font-family:monospace;background:#0d1117;color:#e6edf3;padding:40px;border-radius:12px;max-width:500px;margin:0 auto\">" +
                "<p style=\"color:#58a6ff;font-size:1.1rem;margin:0 0 4px\"><b>{ } ars-prompt</b></p>" +
                "<p style=\"color:#8b949e;margin:0 0 24px;font-size:0.85rem\">Verificacao de conta</p>" +
                "<h2 style=\"margin:0 0 16px;font-size:1.4rem\">Ola, " + escaparHtml(nome) + "!</h2>" +
                "<p style=\"color:#8b949e;line-height:1.7\">Sua conta de <strong style=\"color:#e6edf3\">" + papel + "</strong> foi criada no Ars Prompt.<br>" +
                "Clique no botao abaixo para verificar seu email e ativar o acesso:</p>" +
                "<div style=\"text-align:center;margin:32px 0\">" +
                "<a href=\"" + link + "\" style=\"background:#58a6ff;color:#0d1117;font-weight:700;font-size:1rem;padding:14px 32px;border-radius:8px;text-decoration:none;display:inline-block\">Verificar minha conta</a>" +
                "</div>" +
                "<p style=\"color:#484f58;font-size:0.8rem;border-top:1px solid #30363d;padding-top:16px;margin:0\">" +
                "Link valido por 24 horas. Se nao criou esta conta, ignore este email.</p>" +
                "</div>";

            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            SMTP_HOST);
            props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
            props.put("mail.smtp.ssl.trust",       SMTP_HOST);

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SMTP_USER, "ars-prompt"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino));
            msg.setSubject("ars-prompt - Verifique sua conta", "UTF-8");
            msg.setContent(html, "text/html; charset=UTF-8");
            Transport.send(msg);

            System.out.println("[EMAIL] Link enviado para: " + destino);
        } catch (Exception e) {
            System.err.println("[EMAIL] Erro ao enviar para " + destino + ": " + e.getMessage());
        }
    }

    static void enviarEmailReset(String destino, String nome, String token, boolean isAdmin) {
        try {
            String papel = isAdmin ? "administrador" : "usuario";
            String html =
                "<div style=\"font-family:monospace;background:#0d1117;color:#e6edf3;padding:40px;border-radius:12px;max-width:500px;margin:0 auto\">" +
                "<p style=\"color:#58a6ff;font-size:1.1rem;margin:0 0 4px\"><b>{ } ars-prompt</b></p>" +
                "<p style=\"color:#8b949e;margin:0 0 24px;font-size:0.85rem\">Redefinicao de senha</p>" +
                "<h2 style=\"margin:0 0 16px;font-size:1.4rem\">Ola, " + escaparHtml(nome) + "!</h2>" +
                "<p style=\"color:#8b949e;line-height:1.7\">Recebemos um pedido para redefinir a senha da sua conta de <strong style=\"color:#e6edf3\">" + papel + "</strong>.<br>" +
                "Copie o token abaixo e cole na pagina de redefinicao:</p>" +
                "<div style=\"background:#161b22;border:1px solid #30363d;border-radius:8px;padding:20px;text-align:center;margin:24px 0\">" +
                "<code style=\"font-size:0.9rem;color:#58a6ff;word-break:break-all\">" + token + "</code>" +
                "</div>" +
                "<p style=\"color:#484f58;font-size:0.8rem;border-top:1px solid #30363d;padding-top:16px;margin:0\">" +
                "Token valido por 1 hora. Se nao solicitou isso, ignore este email.</p>" +
                "</div>";

            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            SMTP_HOST);
            props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
            props.put("mail.smtp.ssl.trust",       SMTP_HOST);

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SMTP_USER, "ars-prompt"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino));
            msg.setSubject("ars-prompt - Redefinicao de senha", "UTF-8");
            msg.setContent(html, "text/html; charset=UTF-8");
            Transport.send(msg);
            System.out.println("[EMAIL] Token de reset enviado para: " + destino);
        } catch (Exception e) {
            System.err.println("[EMAIL] Erro ao enviar reset para " + destino + ": " + e.getMessage());
        }
    }

    static String escaparHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // ── Redireciona o browser (302) ──────────────────────────
    static void redirecionar(HttpExchange ex, String url) throws IOException {
        ex.getResponseHeaders().set("Location", url);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    static void respHtml(HttpExchange ex, String html) throws IOException {
        ex.getResponseHeaders().set("Content-Type","text/html; charset=UTF-8");
        byte[]b=html.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200,b.length);
        try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }

    // ════════════════════════════════════════════════════════
    //  DEMAIS ENDPOINTS
    // ════════════════════════════════════════════════════════
    static void handlePrompts(HttpExchange ex) throws Exception {
        String method=ex.getRequestMethod().toUpperCase(),path=ex.getRequestURI().getPath(),query=ex.getRequestURI().getQuery();
        if("GET".equals(method)){
            int uid=qint(query,"uid"); StringBuilder sb=new StringBuilder("[");
            try(PreparedStatement ps=db().prepareStatement("SELECT * FROM prompts WHERE usuario_id=? ORDER BY criado_em DESC")){
                ps.setInt(1,uid);ResultSet rs=ps.executeQuery();boolean first=true;
                while(rs.next()){if(!first)sb.append(",");first=false;sb.append(promptJson(rs));}
            } respJson(ex,200,sb.append("]").toString());return;
        }
        String body=body(ex);
        if("POST".equals(method)){
            int uid=Integer.parseInt(jstr(body,"usuarioId")),catId=Integer.parseInt(jor(body,"categoriaId","0"));
            try(PreparedStatement ps=db().prepareStatement("INSERT INTO prompts (usuario_id,categoria_id,titulo,conteudo) VALUES (?,?,?,?)")){
                ps.setInt(1,uid);if(catId>0)ps.setInt(2,catId);else ps.setNull(2,Types.INTEGER);
                ps.setString(3,jstr(body,"titulo"));ps.setString(4,jstr(body,"conteudo"));ps.executeUpdate();
            } log(uid,"CRIAR_PROMPT","Titulo: "+jstr(body,"titulo"));respText(ex,200,"Prompt criado com sucesso!");return;
        }
        int id=pathId(path);
        if("PUT".equals(method)){
            try(PreparedStatement ps=db().prepareStatement("UPDATE prompts SET titulo=?,conteudo=?,categoria_id=? WHERE id=?")){
                ps.setString(1,jstr(body,"titulo"));ps.setString(2,jstr(body,"conteudo"));
                int cat=Integer.parseInt(jor(body,"categoriaId","0"));if(cat>0)ps.setInt(3,cat);else ps.setNull(3,Types.INTEGER);
                ps.setInt(4,id);ps.executeUpdate();
            } log(0,"EDITAR_PROMPT","ID: "+id);respText(ex,200,"Prompt atualizado!");return;
        }
        if("DELETE".equals(method)){
            try(PreparedStatement ps=db().prepareStatement("DELETE FROM prompts WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();}
            log(0,"DELETAR_PROMPT","ID: "+id);respText(ex,200,"Prompt deletado.");return;
        }
        respText(ex,405,"Metodo nao permitido");
    }

    static void handleCategorias(HttpExchange ex) throws Exception {
        StringBuilder sb=new StringBuilder("[");
        try(PreparedStatement ps=db().prepareStatement("SELECT * FROM categorias ORDER BY nome");ResultSet rs=ps.executeQuery()){
            boolean first=true;
            while(rs.next()){if(!first)sb.append(",");first=false;
                sb.append(String.format("{\"id\":%d,\"nome\":\"%s\",\"descricao\":\"%s\"}",rs.getInt("id"),esc(rs.getString("nome")),esc(rs.getString("descricao"))));}
        } respJson(ex,200,sb.append("]").toString());
    }

    static void handleAdminStats(HttpExchange ex) throws Exception {
        int usuarios=0,ativos=0,prompts=0,cats=0,logs=0;
        try(ResultSet rs=db().prepareStatement("SELECT COUNT(*) FROM usuarios").executeQuery()){if(rs.next())usuarios=rs.getInt(1);}
        try(ResultSet rs=db().prepareStatement("SELECT COUNT(*) FROM usuarios WHERE ativo=1").executeQuery()){if(rs.next())ativos=rs.getInt(1);}
        try(ResultSet rs=db().prepareStatement("SELECT COUNT(*) FROM prompts").executeQuery()){if(rs.next())prompts=rs.getInt(1);}
        try(ResultSet rs=db().prepareStatement("SELECT COUNT(*) FROM categorias").executeQuery()){if(rs.next())cats=rs.getInt(1);}
        try(ResultSet rs=db().prepareStatement("SELECT COUNT(*) FROM historico_logs").executeQuery()){if(rs.next())logs=rs.getInt(1);}
        int[]chart=new int[7];
        try(PreparedStatement ps=db().prepareStatement("SELECT DATE(feito_em) AS dia,COUNT(*) AS total FROM historico_logs WHERE feito_em>=DATE_SUB(CURDATE(),INTERVAL 6 DAY) GROUP BY dia ORDER BY dia")){
            ResultSet rs=ps.executeQuery();
            while(rs.next()){long diff=(System.currentTimeMillis()-rs.getDate("dia").getTime())/86400000L;int pos=6-(int)diff;if(pos>=0&&pos<7)chart[pos]=rs.getInt("total");}
        }
        respJson(ex,200,String.format("{\"usuarios\":%d,\"usuariosAtivos\":%d,\"prompts\":%d,\"categorias\":%d,\"logs\":%d,\"chart\":[%d,%d,%d,%d,%d,%d,%d]}",
            usuarios,ativos,prompts,cats,logs,chart[0],chart[1],chart[2],chart[3],chart[4],chart[5],chart[6]));
    }

    static void handleAdminUsuarios(HttpExchange ex) throws Exception {
        String method=ex.getRequestMethod().toUpperCase(),path=ex.getRequestURI().getPath();
        if("GET".equals(method)){
            StringBuilder sb=new StringBuilder("[");
            try(ResultSet rs=db().prepareStatement("SELECT * FROM usuarios ORDER BY criado_em DESC").executeQuery()){
                boolean first=true;
                while(rs.next()){if(!first)sb.append(",");first=false;
                    sb.append(String.format("{\"id\":%d,\"username\":\"%s\",\"email\":\"%s\",\"ativo\":%d,\"verificado\":%d,\"role\":\"%s\",\"criadoEm\":\"%s\"}",
                        rs.getInt("id"),esc(rs.getString("username")),esc(rs.getString("email")),
                        rs.getInt("ativo"),rs.getInt("verificado"),esc(rs.getString("role")),esc(rs.getString("criado_em"))));}
            } respJson(ex,200,sb.append("]").toString());return;
        }
        if("POST".equals(method)){
            int ativo=path.endsWith("/ativar")?1:0;
            int id=pathId(path.replace("/ativar","").replace("/desativar",""));
            try(PreparedStatement ps=db().prepareStatement("UPDATE usuarios SET ativo=? WHERE id=?")){ps.setInt(1,ativo);ps.setInt(2,id);ps.executeUpdate();}
            logAdmin(1,ativo==1?"ATIVAR_CONTA":"DESATIVAR_CONTA","ID: "+id);
            respText(ex,200,ativo==1?"Conta ativada.":"Conta desativada.");return;
        }
        respText(ex,405,"Metodo nao permitido");
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/admin/tornar-admin — promove usuario a admin
    // ════════════════════════════════════════════════════════
    static void handleTornarAdmin(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body = body(ex);
        String idStr = jstr(body,"id");
        if (idStr.isEmpty()) idStr = jnum(body,"id");
        int id = Integer.parseInt(idStr);

        String username="", email="", senha="";
        try (PreparedStatement ps = db().prepareStatement("SELECT username,email,password FROM usuarios WHERE id=?")) {
            ps.setInt(1,id); ResultSet rs = ps.executeQuery();
            if (!rs.next()) { respText(ex,404,"Usuario nao encontrado."); return; }
            username = rs.getString("username");
            email    = rs.getString("email");
            senha    = rs.getString("password");
        }

        try (PreparedStatement ps = db().prepareStatement(
                "INSERT INTO admins (username,email,password,verificado) VALUES (?,?,?,1)")) {
            ps.setString(1,username); ps.setString(2,email); ps.setString(3,senha);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            respText(ex,400,"Este usuario ja e administrador."); return;
        }

        logAdmin(1,"ADMIN_CRIADO","Promovido de usuario ID: "+id);
        respText(ex,200,"Usuario \""+username+"\" promovido a administrador!");
    }

    // ════════════════════════════════════════════════════════
    //  DELETE /api/admin/deletar-usuario/{id}
    // ════════════════════════════════════════════════════════
    static void handleDeletarUsuario(HttpExchange ex) throws Exception {
        if (!"DELETE".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        int id = pathId(ex.getRequestURI().getPath());
        if (id <= 0) { respText(ex,400,"ID invalido."); return; }

        String username = "";
        try (PreparedStatement ps = db().prepareStatement("SELECT username FROM usuarios WHERE id=?")) {
            ps.setInt(1,id); ResultSet rs = ps.executeQuery();
            if (!rs.next()) { respText(ex,404,"Usuario nao encontrado."); return; }
            username = rs.getString("username");
        }

        try (PreparedStatement ps = db().prepareStatement("DELETE FROM usuarios WHERE id=?")) {
            ps.setInt(1,id); ps.executeUpdate();
        }
        logAdmin(1,"DESATIVAR_CONTA","Conta deletada: "+username+" (ID: "+id+")");
        respText(ex,200,"Conta de \""+username+"\" deletada com sucesso.");
    }

    static void handleAdminPrompts(HttpExchange ex) throws Exception {
        String method=ex.getRequestMethod().toUpperCase(),path=ex.getRequestURI().getPath();
        if("GET".equals(method)){
            StringBuilder sb=new StringBuilder("[");
            try(PreparedStatement ps=db().prepareStatement("SELECT p.*,u.username FROM prompts p JOIN usuarios u ON p.usuario_id=u.id ORDER BY p.criado_em DESC");ResultSet rs=ps.executeQuery()){
                boolean first=true;while(rs.next()){if(!first)sb.append(",");first=false;sb.append(promptJson(rs));}
            } respJson(ex,200,sb.append("]").toString());return;
        }
        int id=pathId(path);String body=body(ex);
        if("PUT".equals(method)){
            try(PreparedStatement ps=db().prepareStatement("UPDATE prompts SET titulo=?,conteudo=?,categoria_id=? WHERE id=?")){
                ps.setString(1,jstr(body,"titulo"));ps.setString(2,jstr(body,"conteudo"));
                int cat=Integer.parseInt(jor(body,"categoriaId","0"));if(cat>0)ps.setInt(3,cat);else ps.setNull(3,Types.INTEGER);
                ps.setInt(4,id);ps.executeUpdate();
            } logAdmin(1,"ADMIN_EDITAR_PROMPT","ID: "+id);respText(ex,200,"Prompt atualizado.");return;
        }
        if("DELETE".equals(method)){
            try(PreparedStatement ps=db().prepareStatement("DELETE FROM prompts WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();}
            logAdmin(1,"ADMIN_DELETAR_PROMPT","ID: "+id);respText(ex,200,"Prompt deletado.");return;
        }
        respText(ex,405,"Metodo nao permitido");
    }

    static void handleAdminCategorias(HttpExchange ex) throws Exception {
        String method=ex.getRequestMethod().toUpperCase(),path=ex.getRequestURI().getPath();
        String body="GET".equals(method)?"":body(ex);
        if("GET".equals(method)){handleCategorias(ex);return;}
        if("POST".equals(method)){
            try(PreparedStatement ps=db().prepareStatement("INSERT INTO categorias (nome,descricao) VALUES (?,?)")){
                ps.setString(1,jstr(body,"nome"));ps.setString(2,jor(body,"descricao",""));ps.executeUpdate();}
            logAdmin(1,"CRIAR_CATEGORIA",jstr(body,"nome"));respText(ex,200,"Categoria criada.");return;
        }
        int id=pathId(path);
        if("PUT".equals(method)){
            try(PreparedStatement ps=db().prepareStatement("UPDATE categorias SET nome=?,descricao=? WHERE id=?")){
                ps.setString(1,jstr(body,"nome"));ps.setString(2,jor(body,"descricao",""));ps.setInt(3,id);ps.executeUpdate();}
            logAdmin(1,"EDITAR_CATEGORIA","ID: "+id);respText(ex,200,"Categoria atualizada.");return;
        }
        if("DELETE".equals(method)){
            try(PreparedStatement ps=db().prepareStatement("DELETE FROM categorias WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();}
            logAdmin(1,"DELETAR_CATEGORIA","ID: "+id);respText(ex,200,"Categoria deletada.");return;
        }
        respText(ex,405,"Metodo nao permitido");
    }

    static void handleAdminLogs(HttpExchange ex) throws Exception {
        StringBuilder sb=new StringBuilder("[");
        try(PreparedStatement ps=db().prepareStatement("SELECT l.*,u.username FROM historico_logs l LEFT JOIN usuarios u ON l.usuario_id=u.id ORDER BY l.feito_em DESC LIMIT 200");ResultSet rs=ps.executeQuery()){
            boolean first=true;
            while(rs.next()){if(!first)sb.append(",");first=false;String user=rs.getString("username");
                sb.append(String.format("{\"id\":%d,\"acao\":\"%s\",\"usuario\":\"%s\",\"detalhes\":\"%s\",\"feito_em\":\"%s\"}",
                    rs.getInt("id"),esc(rs.getString("acao")),esc(user==null?"admin":user),esc(rs.getString("detalhes")),esc(rs.getString("feito_em"))));}
        } respJson(ex,200,sb.append("]").toString());
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    static void log(int uid,String acao,String det){
        try(PreparedStatement ps=db().prepareStatement("INSERT INTO historico_logs (usuario_id,acao,detalhes) VALUES (?,?,?)")){
            if(uid>0)ps.setInt(1,uid);else ps.setNull(1,Types.INTEGER);
            ps.setString(2,acao);ps.setString(3,det);ps.executeUpdate();
        }catch(Exception e){System.err.println("[LOG] "+e.getMessage());}
    }
    static void logAdmin(int adminId,String acao,String det){
        try(PreparedStatement ps=db().prepareStatement("INSERT INTO historico_logs (admin_id,acao,detalhes) VALUES (?,?,?)")){
            ps.setInt(1,adminId);ps.setString(2,acao);ps.setString(3,det);ps.executeUpdate();
        }catch(Exception e){System.err.println("[LOG] "+e.getMessage());}
    }
    static String promptJson(ResultSet rs) throws SQLException {
        return String.format("{\"id\":%d,\"usuarioId\":%d,\"categoriaId\":%d,\"titulo\":\"%s\",\"conteudo\":\"%s\",\"criadoEm\":\"%s\",\"atualizadoEm\":\"%s\"}",
            rs.getInt("id"),rs.getInt("usuario_id"),rs.getInt("categoria_id"),
            esc(rs.getString("titulo")),esc(rs.getString("conteudo")),
            esc(rs.getString("criado_em")),esc(rs.getString("atualizado_em")));
    }
    static void cors(HttpExchange ex){
        ex.getResponseHeaders().add("Access-Control-Allow-Origin","*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers","Content-Type");
    }
    static void respText(HttpExchange ex,int s,String msg) throws IOException {
        ex.getResponseHeaders().set("Content-Type","text/plain; charset=UTF-8");
        byte[]b=msg.getBytes(StandardCharsets.UTF_8);ex.sendResponseHeaders(s,b.length);
        try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }
    static void respJson(HttpExchange ex,int s,String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        byte[]b=json.getBytes(StandardCharsets.UTF_8);ex.sendResponseHeaders(s,b.length);
        try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }
    static String body(HttpExchange ex) throws IOException {
        ByteArrayOutputStream buf=new ByteArrayOutputStream();byte[]data=new byte[1024];int n;
        while((n=ex.getRequestBody().read(data))!=-1)buf.write(data,0,n);
        return buf.toString(StandardCharsets.UTF_8.name());
    }
    static String jstr(String body,String campo){
        try{int idx=body.indexOf("\""+campo+"\"");if(idx<0)return "";
            int colon=body.indexOf(":",idx);int q1=body.indexOf("\"",colon+1);if(q1<0)return "";
            int q2=body.indexOf("\"",q1+1);return body.substring(q1+1,q2);
        }catch(Exception e){return "";}
    }
    static String jnum(String body, String campo) {
        try {
            int idx = body.indexOf("\""+campo+"\"");
            if (idx < 0) return "";
            int colon = body.indexOf(":", idx);
            StringBuilder num = new StringBuilder();
            for (int i = colon+1; i < body.length(); i++) {
                char c = body.charAt(i);
                if (Character.isDigit(c)) num.append(c);
                else if (c == ' ' || c == '\n') { if (num.length()==0) continue; else break; }
                else break;
            }
            return num.toString();
        } catch(Exception e) { return ""; }
    }
    static String jor(String body,String campo,String def){String v=jstr(body,campo);return(v==null||v.isEmpty())?def:v;}
    static int pathId(String path){String[]p=path.split("/");for(int i=p.length-1;i>=0;i--){try{return Integer.parseInt(p[i]);}catch(NumberFormatException ignored){}}return -1;}
    static int qint(String q,String p){if(q==null)return 0;for(String par:q.split("&")){String[]kv=par.split("=");if(kv.length==2&&kv[0].equals(p)){try{return Integer.parseInt(kv[1]);}catch(Exception e){return 0;}}}return 0;}
    static String qstr(String q,String p){if(q==null)return "";for(String par:q.split("&")){String[]kv=par.split("=",2);if(kv.length==2&&kv[0].equals(p))return kv[1];}return "";}
    static String esc(String s){return s==null?"":s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","");}
    static void validarEmail(String e) throws EmailException{if(e==null||!e.contains("@")||!e.contains("."))throw new EmailException("Email invalido: "+e);}
    static void validarSenha(String s) throws SenhaException{if(s==null||s.length()<9)throw new SenhaException("Senha deve ter no minimo 9 caracteres.");}
    static String sha256(String texto){
        try{MessageDigest md=MessageDigest.getInstance("SHA-256");byte[]hash=md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb=new StringBuilder();for(byte b:hash)sb.append(String.format("%02x",b));return sb.toString();
        }catch(Exception e){throw new RuntimeException("Erro SHA-256",e);}
    }
    static void migrarSenhas(){
        try{ResultSet rs=db().prepareStatement("SELECT id, password FROM usuarios").executeQuery();int m=0;
            while(rs.next()){String pwd=rs.getString("password");
                if(pwd!=null&&pwd.length()<60){String hash=sha256(pwd);
                    try(PreparedStatement ps=db().prepareStatement("UPDATE usuarios SET password=? WHERE id=?")){ps.setString(1,hash);ps.setInt(2,rs.getInt("id"));ps.executeUpdate();m++;}
                }
            }
            if(m>0)System.out.println("[DB] "+m+" senha(s) migrada(s) para SHA-256.");
        }catch(Exception e){System.err.println("[AVISO] Erro na migracao: "+e.getMessage());}
    }
}
