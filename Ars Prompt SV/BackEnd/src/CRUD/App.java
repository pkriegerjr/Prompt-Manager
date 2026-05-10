import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

// ══════════════════════════════════════════════════════════════
//  ARS PROMPT — Servidor completo em arquivo único
//  Compile: javac -cp ".;mysql-connector-j-X.X.X.jar" App.java EmailException.java SenhaException.java
//  Rode:    java  -cp ".;mysql-connector-j-X.X.X.jar" App
//  (Linux/Mac: troque ; por : )
// ══════════════════════════════════════════════════════════════
public class App {

    // ── Configuração do banco ────────────────────────────────
    static final String DB_URL  = "jdbc:mysql://localhost:3306/ars_database?useSSL=false&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";
    static final String DB_USER = "root";
    static final String DB_PASS = "";

    static Connection conn;

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
    //  MAIN — sobe o servidor HTTP na porta 8081
    // ════════════════════════════════════════════════════════
    public static void main(String[] args) throws Exception {
        // Testa conexão antes de subir
        try {
            db();
            System.out.println("[DB] Banco ars_database conectado!");
            migrarSenhas();  // converte senhas em texto puro para SHA-256
        } catch (SQLException e) {
            System.err.println("[ERRO] Nao foi possivel conectar ao banco:");
            System.err.println(e.getMessage());
            System.err.println(">>> Verifique se o MySQL do XAMPP esta rodando.");
            System.exit(1);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/api/usuarios",         ex -> rotear(ex, App::handleUsuarios));
        server.createContext("/api/login",            ex -> rotear(ex, App::handleLogin));
        server.createContext("/api/prompts",          ex -> rotear(ex, App::handlePrompts));
        server.createContext("/api/categorias",       ex -> rotear(ex, App::handleCategorias));
        server.createContext("/api/admin/stats",      ex -> rotear(ex, App::handleAdminStats));
        server.createContext("/api/admin/usuarios",   ex -> rotear(ex, App::handleAdminUsuarios));
        server.createContext("/api/admin/prompts",    ex -> rotear(ex, App::handleAdminPrompts));
        server.createContext("/api/admin/categorias", ex -> rotear(ex, App::handleAdminCategorias));
        server.createContext("/api/admin/logs",       ex -> rotear(ex, App::handleAdminLogs));
        server.setExecutor(null);

        System.out.println("========================================");
        System.out.println("  Ars Prompt rodando em localhost:8081");
        System.out.println("========================================");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { if (conn!=null&&!conn.isClosed()) conn.close(); } catch(Exception ignored){}
        }));
    }

    // ── Wrapper CORS + roteamento ────────────────────────────
    interface Handler { void handle(HttpExchange ex) throws Exception; }

    static void rotear(HttpExchange ex, Handler h) throws IOException {
        cors(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { ex.sendResponseHeaders(204,-1); ex.close(); return; }
        try { h.handle(ex); }
        catch (Exception e) { System.err.println("[ERRO] "+e.getMessage()); respText(ex,500,"Erro interno: "+e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/usuarios — Cadastro
    // ════════════════════════════════════════════════════════
    static void handleUsuarios(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body  = body(ex);
        String user  = jstr(body,"username");
        String email = jstr(body,"email");
        String senha = jstr(body,"password");

        validarEmail(email);
        validarSenha(senha);

        try (PreparedStatement ps = db().prepareStatement(
                "INSERT INTO usuarios (username, email, password) VALUES (?,?,?)")) {
            ps.setString(1,user); ps.setString(2,email); ps.setString(3,sha256(senha));
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            respText(ex,400,"Email ou nome de usuario ja cadastrado."); return;
        }

        log(0,"CADASTRO","Novo usuario: "+user);
        respText(ex,200,"Conta criada com sucesso! Bem-vindo, "+user);
    }

    // ════════════════════════════════════════════════════════
    //  POST /api/login — Login usuario ou admin
    // ════════════════════════════════════════════════════════
    static void handleLogin(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { respText(ex,405,"Metodo nao permitido"); return; }
        String body  = body(ex);
        String email = jstr(body,"email");
        String senha = jstr(body,"password");

        // Admin padrão (substituir por tabela admins futuramente)
        if ("admin@arsprompt.local".equals(email) && "admin123".equals(senha)) {
            respJson(ex,200,"{\"tipo\":\"admin\",\"id\":1,\"username\":\"admin\"}"); return;
        }

        // Busca usuario no banco
        try (PreparedStatement ps = db().prepareStatement("SELECT * FROM usuarios WHERE email=?")) {
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (!rs.getString("password").equals(sha256(senha))) { respText(ex,401,"E-mail ou senha incorretos."); return; }
                if (rs.getInt("ativo")==0) {
                    log(rs.getInt("id"),"ERRO_LOGIN","Conta desativada");
                    respText(ex,403,"Conta desativada. Contate o administrador."); return;
                }
                log(rs.getInt("id"),"LOGIN",null);
                respJson(ex,200,String.format(
                    "{\"tipo\":\"usuario\",\"id\":%d,\"username\":\"%s\"}",
                    rs.getInt("id"), esc(rs.getString("username"))));
                return;
            }
        }
        respText(ex,401,"E-mail ou senha incorretos.");
    }

    // ════════════════════════════════════════════════════════
    //  /api/prompts — CRUD de prompts
    // ════════════════════════════════════════════════════════
    static void handlePrompts(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();
        String query  = ex.getRequestURI().getQuery();

        // GET /api/prompts?uid=X
        if ("GET".equals(method)) {
            int uid = qint(query,"uid");
            StringBuilder sb = new StringBuilder("[");
            try (PreparedStatement ps = db().prepareStatement(
                    "SELECT * FROM prompts WHERE usuario_id=? ORDER BY criado_em DESC")) {
                ps.setInt(1,uid);
                ResultSet rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(","); first=false;
                    sb.append(promptJson(rs));
                }
            }
            respJson(ex,200,sb.append("]").toString()); return;
        }

        String body = body(ex);

        // POST — criar
        if ("POST".equals(method)) {
            int uid    = Integer.parseInt(jstr(body,"usuarioId"));
            int catId  = Integer.parseInt(jor(body,"categoriaId","0"));
            String tit = jstr(body,"titulo");
            String con = jstr(body,"conteudo");
            try (PreparedStatement ps = db().prepareStatement(
                    "INSERT INTO prompts (usuario_id,categoria_id,titulo,conteudo) VALUES (?,?,?,?)")) {
                ps.setInt(1,uid);
                if (catId>0) ps.setInt(2,catId); else ps.setNull(2,Types.INTEGER);
                ps.setString(3,tit); ps.setString(4,con);
                ps.executeUpdate();
            }
            log(uid,"CRIAR_PROMPT","Titulo: "+tit);
            respText(ex,200,"Prompt criado com sucesso!"); return;
        }

        int id = pathId(path);

        // PUT — editar
        if ("PUT".equals(method)) {
            try (PreparedStatement ps = db().prepareStatement(
                    "UPDATE prompts SET titulo=?,conteudo=?,categoria_id=? WHERE id=?")) {
                ps.setString(1,jstr(body,"titulo"));
                ps.setString(2,jstr(body,"conteudo"));
                int catId = Integer.parseInt(jor(body,"categoriaId","0"));
                if (catId>0) ps.setInt(3,catId); else ps.setNull(3,Types.INTEGER);
                ps.setInt(4,id);
                ps.executeUpdate();
            }
            log(0,"EDITAR_PROMPT","ID: "+id);
            respText(ex,200,"Prompt atualizado!"); return;
        }

        // DELETE
        if ("DELETE".equals(method)) {
            try (PreparedStatement ps = db().prepareStatement("DELETE FROM prompts WHERE id=?")) {
                ps.setInt(1,id); ps.executeUpdate();
            }
            log(0,"DELETAR_PROMPT","ID: "+id);
            respText(ex,200,"Prompt deletado."); return;
        }

        respText(ex,405,"Metodo nao permitido");
    }

    // ════════════════════════════════════════════════════════
    //  GET /api/categorias
    // ════════════════════════════════════════════════════════
    static void handleCategorias(HttpExchange ex) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        try (PreparedStatement ps = db().prepareStatement("SELECT * FROM categorias ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(","); first=false;
                sb.append(String.format("{\"id\":%d,\"nome\":\"%s\",\"descricao\":\"%s\"}",
                    rs.getInt("id"), esc(rs.getString("nome")), esc(rs.getString("descricao"))));
            }
        }
        respJson(ex,200,sb.append("]").toString());
    }

    // ════════════════════════════════════════════════════════
    //  GET /api/admin/stats — métricas do dashboard
    // ════════════════════════════════════════════════════════
    static void handleAdminStats(HttpExchange ex) throws Exception {
        int usuarios=0, ativos=0, prompts=0, cats=0, logs=0;
        try (ResultSet rs = db().prepareStatement("SELECT COUNT(*) FROM usuarios").executeQuery()) { if(rs.next()) usuarios=rs.getInt(1); }
        try (ResultSet rs = db().prepareStatement("SELECT COUNT(*) FROM usuarios WHERE ativo=1").executeQuery()) { if(rs.next()) ativos=rs.getInt(1); }
        try (ResultSet rs = db().prepareStatement("SELECT COUNT(*) FROM prompts").executeQuery()) { if(rs.next()) prompts=rs.getInt(1); }
        try (ResultSet rs = db().prepareStatement("SELECT COUNT(*) FROM categorias").executeQuery()) { if(rs.next()) cats=rs.getInt(1); }
        try (ResultSet rs = db().prepareStatement("SELECT COUNT(*) FROM historico_logs").executeQuery()) { if(rs.next()) logs=rs.getInt(1); }

        // Atividade últimos 7 dias
        int[] chart = new int[7];
        try (PreparedStatement ps = db().prepareStatement(
                "SELECT DATE(feito_em) AS dia, COUNT(*) AS total FROM historico_logs " +
                "WHERE feito_em >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY dia ORDER BY dia")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long diff = (System.currentTimeMillis() - rs.getDate("dia").getTime()) / 86400000L;
                int pos = 6-(int)diff;
                if (pos>=0&&pos<7) chart[pos]=rs.getInt("total");
            }
        }

        respJson(ex,200,String.format(
            "{\"usuarios\":%d,\"usuariosAtivos\":%d,\"prompts\":%d,\"categorias\":%d,\"logs\":%d,\"chart\":[%d,%d,%d,%d,%d,%d,%d]}",
            usuarios,ativos,prompts,cats,logs,
            chart[0],chart[1],chart[2],chart[3],chart[4],chart[5],chart[6]));
    }

    // ════════════════════════════════════════════════════════
    //  /api/admin/usuarios
    // ════════════════════════════════════════════════════════
    static void handleAdminUsuarios(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();

        if ("GET".equals(method)) {
            StringBuilder sb = new StringBuilder("[");
            try (ResultSet rs = db().prepareStatement("SELECT * FROM usuarios ORDER BY criado_em DESC").executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(","); first=false;
                    sb.append(String.format(
                        "{\"id\":%d,\"username\":\"%s\",\"email\":\"%s\",\"ativo\":%d,\"role\":\"%s\",\"criadoEm\":\"%s\"}",
                        rs.getInt("id"),esc(rs.getString("username")),esc(rs.getString("email")),
                        rs.getInt("ativo"),esc(rs.getString("role")),esc(rs.getString("criado_em"))));
                }
            }
            respJson(ex,200,sb.append("]").toString()); return;
        }

        if ("POST".equals(method)) {
            int ativo = path.endsWith("/ativar") ? 1 : 0;
            String limpo = path.replace("/ativar","").replace("/desativar","");
            int id = pathId(limpo);
            try (PreparedStatement ps = db().prepareStatement("UPDATE usuarios SET ativo=? WHERE id=?")) {
                ps.setInt(1,ativo); ps.setInt(2,id); ps.executeUpdate();
            }
            logAdmin(1, ativo==1?"ATIVAR_CONTA":"DESATIVAR_CONTA","ID: "+id);
            respText(ex,200, ativo==1?"Conta ativada.":"Conta desativada."); return;
        }

        respText(ex,405,"Metodo nao permitido");
    }

    // ════════════════════════════════════════════════════════
    //  /api/admin/prompts
    // ════════════════════════════════════════════════════════
    static void handleAdminPrompts(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();

        if ("GET".equals(method)) {
            StringBuilder sb = new StringBuilder("[");
            try (PreparedStatement ps = db().prepareStatement(
                    "SELECT p.*,u.username FROM prompts p JOIN usuarios u ON p.usuario_id=u.id ORDER BY p.criado_em DESC");
                 ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) { if(!first)sb.append(","); first=false; sb.append(promptJson(rs)); }
            }
            respJson(ex,200,sb.append("]").toString()); return;
        }

        int id = pathId(path);
        String body = body(ex);

        if ("PUT".equals(method)) {
            try (PreparedStatement ps = db().prepareStatement(
                    "UPDATE prompts SET titulo=?,conteudo=?,categoria_id=? WHERE id=?")) {
                ps.setString(1,jstr(body,"titulo")); ps.setString(2,jstr(body,"conteudo"));
                int cat=Integer.parseInt(jor(body,"categoriaId","0"));
                if(cat>0)ps.setInt(3,cat);else ps.setNull(3,Types.INTEGER);
                ps.setInt(4,id); ps.executeUpdate();
            }
            logAdmin(1,"ADMIN_EDITAR_PROMPT","ID: "+id);
            respText(ex,200,"Prompt atualizado."); return;
        }

        if ("DELETE".equals(method)) {
            try (PreparedStatement ps = db().prepareStatement("DELETE FROM prompts WHERE id=?")) {
                ps.setInt(1,id); ps.executeUpdate();
            }
            logAdmin(1,"ADMIN_DELETAR_PROMPT","ID: "+id);
            respText(ex,200,"Prompt deletado."); return;
        }

        respText(ex,405,"Metodo nao permitido");
    }

    // ════════════════════════════════════════════════════════
    //  /api/admin/categorias
    // ════════════════════════════════════════════════════════
    static void handleAdminCategorias(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();
        String body   = "GET".equals(method) ? "" : body(ex);

        if ("GET".equals(method)) { handleCategorias(ex); return; }

        if ("POST".equals(method)) {
            try (PreparedStatement ps = db().prepareStatement("INSERT INTO categorias (nome,descricao) VALUES (?,?)")) {
                ps.setString(1,jstr(body,"nome")); ps.setString(2,jor(body,"descricao",""));
                ps.executeUpdate();
            }
            logAdmin(1,"CRIAR_CATEGORIA",jstr(body,"nome"));
            respText(ex,200,"Categoria criada."); return;
        }

        int id = pathId(path);

        if ("PUT".equals(method)) {
            try (PreparedStatement ps = db().prepareStatement("UPDATE categorias SET nome=?,descricao=? WHERE id=?")) {
                ps.setString(1,jstr(body,"nome")); ps.setString(2,jor(body,"descricao","")); ps.setInt(3,id);
                ps.executeUpdate();
            }
            logAdmin(1,"EDITAR_CATEGORIA","ID: "+id);
            respText(ex,200,"Categoria atualizada."); return;
        }

        if ("DELETE".equals(method)) {
            try (PreparedStatement ps = db().prepareStatement("DELETE FROM categorias WHERE id=?")) {
                ps.setInt(1,id); ps.executeUpdate();
            }
            logAdmin(1,"DELETAR_CATEGORIA","ID: "+id);
            respText(ex,200,"Categoria deletada."); return;
        }

        respText(ex,405,"Metodo nao permitido");
    }

    // ════════════════════════════════════════════════════════
    //  GET /api/admin/logs
    // ════════════════════════════════════════════════════════
    static void handleAdminLogs(HttpExchange ex) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        try (PreparedStatement ps = db().prepareStatement(
                "SELECT l.*,u.username FROM historico_logs l LEFT JOIN usuarios u ON l.usuario_id=u.id ORDER BY l.feito_em DESC LIMIT 200");
             ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(","); first=false;
                String user = rs.getString("username");
                sb.append(String.format(
                    "{\"id\":%d,\"acao\":\"%s\",\"usuario\":\"%s\",\"detalhes\":\"%s\",\"feito_em\":\"%s\"}",
                    rs.getInt("id"),esc(rs.getString("acao")),
                    esc(user==null?"admin":user),
                    esc(rs.getString("detalhes")),esc(rs.getString("feito_em"))));
            }
        }
        respJson(ex,200,sb.append("]").toString());
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    static void log(int uid, String acao, String det) {
        try (PreparedStatement ps = db().prepareStatement(
                "INSERT INTO historico_logs (usuario_id,acao,detalhes) VALUES (?,?,?)")) {
            if(uid>0)ps.setInt(1,uid);else ps.setNull(1,Types.INTEGER);
            ps.setString(2,acao); ps.setString(3,det); ps.executeUpdate();
        } catch(Exception e){ System.err.println("[LOG] "+e.getMessage()); }
    }

    static void logAdmin(int adminId, String acao, String det) {
        try (PreparedStatement ps = db().prepareStatement(
                "INSERT INTO historico_logs (admin_id,acao,detalhes) VALUES (?,?,?)")) {
            ps.setInt(1,adminId); ps.setString(2,acao); ps.setString(3,det); ps.executeUpdate();
        } catch(Exception e){ System.err.println("[LOG] "+e.getMessage()); }
    }

    static String promptJson(ResultSet rs) throws SQLException {
        return String.format(
            "{\"id\":%d,\"usuarioId\":%d,\"categoriaId\":%d,\"titulo\":\"%s\",\"conteudo\":\"%s\",\"criadoEm\":\"%s\",\"atualizadoEm\":\"%s\"}",
            rs.getInt("id"),rs.getInt("usuario_id"),rs.getInt("categoria_id"),
            esc(rs.getString("titulo")),esc(rs.getString("conteudo")),
            esc(rs.getString("criado_em")),esc(rs.getString("atualizado_em")));
    }

    static void cors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin","*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers","Content-Type");
    }

    static void respText(HttpExchange ex, int s, String msg) throws IOException {
        ex.getResponseHeaders().set("Content-Type","text/plain; charset=UTF-8");
        byte[] b=msg.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(s,b.length);
        try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }

    static void respJson(HttpExchange ex, int s, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        byte[] b=json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(s,b.length);
        try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }

    static String body(HttpExchange ex) throws IOException {
        ByteArrayOutputStream buf=new ByteArrayOutputStream();
        byte[] data=new byte[1024]; int n;
        while((n=ex.getRequestBody().read(data))!=-1)buf.write(data,0,n);
        return buf.toString(StandardCharsets.UTF_8.name());
    }

    static String jstr(String body, String campo) {
        try {
            int idx=body.indexOf("\""+campo+"\""); if(idx<0)return "";
            int colon=body.indexOf(":",idx);
            int q1=body.indexOf("\"",colon+1); if(q1<0)return "";
            int q2=body.indexOf("\"",q1+1);
            return body.substring(q1+1,q2);
        } catch(Exception e){return "";}
    }

    static String jor(String body,String campo,String def){String v=jstr(body,campo);return(v==null||v.isEmpty())?def:v;}

    static int pathId(String path) {
        String[]p=path.split("/");
        for(int i=p.length-1;i>=0;i--){try{return Integer.parseInt(p[i]);}catch(NumberFormatException ignored){}}
        return -1;
    }

    static int qint(String q,String p){
        if(q==null)return 0;
        for(String par:q.split("&")){String[]kv=par.split("=");if(kv.length==2&&kv[0].equals(p)){try{return Integer.parseInt(kv[1]);}catch(Exception e){return 0;}}}
        return 0;
    }

    static String esc(String s){return s==null?"":s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","");}

    static void validarEmail(String e) throws EmailException {
        if(e==null||!e.contains("@")||!e.contains("."))throw new EmailException("Email invalido: "+e);
    }
    static void validarSenha(String s) throws SenhaException {
        if(s==null||s.length()<9)throw new SenhaException("Senha deve ter no minimo 9 caracteres.");
    }

    // ── SHA-256: criptografia simples sem lib externa ────────
    static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash SHA-256", e);
        }
    }

    // ── Migra senhas em texto puro para SHA-256 ──────────────
    // Chamado uma vez ao iniciar o servidor.
    // Se a senha no banco tiver menos de 60 chars, não é hash ainda.
    static void migrarSenhas() {
        try {
            ResultSet rs = db().prepareStatement("SELECT id, password FROM usuarios").executeQuery();
            int migrados = 0;
            while (rs.next()) {
                String pwd = rs.getString("password");
                if (pwd != null && pwd.length() < 60) {  // texto puro detectado
                    String hash = sha256(pwd);
                    try (PreparedStatement ps = db().prepareStatement(
                            "UPDATE usuarios SET password=? WHERE id=?")) {
                        ps.setString(1, hash);
                        ps.setInt(2, rs.getInt("id"));
                        ps.executeUpdate();
                        migrados++;
                    }
                }
            }
            if (migrados > 0)
                System.out.println("[DB] " + migrados + " senha(s) migrada(s) para SHA-256.");
        } catch (Exception e) {
            System.err.println("[AVISO] Erro na migracao de senhas: " + e.getMessage());
        }
    }
}
