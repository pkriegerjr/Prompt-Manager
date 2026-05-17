package controller;

import com.sun.net.httpserver.HttpExchange;
import dao.AdminDao;
import dao.LogDao;
import dao.UsuarioDao;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.UUID;
import model.Admin;
import model.Usuario;
import service.EmailService;
import service.VerificationPage;
import util.HttpUtil;
import util.JsonUtil;
import util.SecurityUtil;

public final class AuthController {
    private AuthController() {}

    public static void usuarios(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String body = HttpUtil.body(ex);
        String user = JsonUtil.str(body,"username");
        String email = JsonUtil.str(body,"email");
        String senha = JsonUtil.str(body,"password");

        try {
            SecurityUtil.validarEmail(email);
            SecurityUtil.validarSenha(senha);
        } catch (IllegalArgumentException e) {
            HttpUtil.text(ex,400,e.getMessage()); return;
        }

        String token = UUID.randomUUID().toString();
        try {
            UsuarioDao.criar(user, email, SecurityUtil.sha256(senha), token);
        } catch (SQLIntegrityConstraintViolationException e) {
            HttpUtil.text(ex,400,"Email ou nome de usuario ja cadastrado."); return;
        }

        new Thread(() -> EmailService.enviarLinkVerificacao(email, user, token, false)).start();
        LogDao.registrarUsuario(0,"CADASTRO","Novo usuario: " + user);
        HttpUtil.text(ex,200,"Conta criada! Enviamos um link de verificacao para " + email);
    }

    public static void verificar(HttpExchange ex) throws Exception {
        String query = ex.getRequestURI().getQuery();
        String token = JsonUtil.queryString(query, "token");
        String tipo = JsonUtil.queryString(query, "tipo");

        if (token.isEmpty()) {
            HttpUtil.html(ex, VerificationPage.resultado("Link invalido", "Token ausente ou malformado.", false)); return;
        }
        if ("admin".equals(tipo)) {
            HttpUtil.html(ex, VerificationPage.resultado("Acesso liberado!", "Administradores nao precisam verificar email. Faca login normalmente.", true)); return;
        }

        Usuario usuario = UsuarioDao.buscarPorTokenVerificacao(token);
        if (usuario == null) {
            HttpUtil.html(ex, VerificationPage.resultado("Link invalido", "Este link ja foi utilizado ou nao existe.", false)); return;
        }
        if (usuario.getVerificado() == 1) {
            HttpUtil.html(ex, VerificationPage.resultado("Ja verificado!", "Sua conta ja estava verificada. Voce ja pode fazer login.", true)); return;
        }
        Timestamp expira = usuario.getTokenExpira();
        if (expira != null && expira.before(new Timestamp(System.currentTimeMillis()))) {
            HttpUtil.html(ex, VerificationPage.resultado("Link expirado", "O link de verificacao expirou. Faca login para receber um novo link.", false)); return;
        }

        UsuarioDao.verificarEmail(usuario.getId());
        LogDao.registrarUsuario(usuario.getId(), "EMAIL_VERIFICADO", "via link");
        HttpUtil.html(ex, VerificationPage.resultado("Email verificado!", "Sua conta esta ativa. Clique abaixo para fazer login.", true));
    }

    public static void reenviar(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String email = JsonUtil.str(HttpUtil.body(ex),"email");
        Usuario usuario = UsuarioDao.buscarPorEmail(email);
        if (usuario == null) { HttpUtil.text(ex,404,"Email nao encontrado."); return; }
        if (usuario.getVerificado() == 1) { HttpUtil.text(ex,200,"Conta ja verificada! Faca login."); return; }

        String token = UUID.randomUUID().toString();
        UsuarioDao.atualizarTokenVerificacao(email, token);
        new Thread(() -> EmailService.enviarLinkVerificacao(email, usuario.getUsername(), token, false)).start();
        HttpUtil.text(ex,200,"Novo link enviado para " + email + ". Verifique sua caixa de entrada.");
    }

    public static void esqueciSenha(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String body = HttpUtil.body(ex);
        String email = JsonUtil.str(body,"email");
        String tipo = JsonUtil.or(body,"tipo","usuario");
        boolean isAdmin = "admin".equals(tipo);
        String token = UUID.randomUUID().toString();
        String username;

        if (isAdmin) {
            Admin admin = AdminDao.buscarPorEmail(email);
            if (admin == null) { HttpUtil.text(ex,404,"Email nao encontrado."); return; }
            username = admin.getUsername();
            AdminDao.atualizarTokenReset(email, token);
        } else {
            Usuario usuario = UsuarioDao.buscarPorEmail(email);
            if (usuario == null) { HttpUtil.text(ex,404,"Email nao encontrado."); return; }
            username = usuario.getUsername();
            UsuarioDao.atualizarTokenReset(email, token);
        }

        new Thread(() -> EmailService.enviarEmailReset(email, username, token, isAdmin)).start();
        HttpUtil.text(ex,200,"Link de redefinicao enviado para " + email + ". Verifique sua caixa de entrada.");
    }

    public static void redefinirSenha(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String body = HttpUtil.body(ex);
        String email = JsonUtil.str(body,"email");
        String token = JsonUtil.str(body,"token");
        String novaSenha = JsonUtil.str(body,"novaSenha");
        String tipo = JsonUtil.or(body,"tipo","usuario");
        boolean isAdmin = "admin".equals(tipo);

        if (novaSenha.length() < 9) { HttpUtil.text(ex,400,"Senha deve ter no minimo 9 caracteres."); return; }

        if (isAdmin) {
            Admin admin = AdminDao.buscarPorEmail(email);
            if (admin == null) { HttpUtil.text(ex,404,"Email nao encontrado."); return; }
            if (!tokenValido(admin.getTokenVerificacao(), admin.getTokenExpira(), token, ex)) return;
            AdminDao.atualizarSenhaPorEmail(email, SecurityUtil.sha256(novaSenha));
        } else {
            Usuario usuario = UsuarioDao.buscarPorEmail(email);
            if (usuario == null) { HttpUtil.text(ex,404,"Email nao encontrado."); return; }
            if (!tokenValido(usuario.getTokenVerificacao(), usuario.getTokenExpira(), token, ex)) return;
            UsuarioDao.atualizarSenhaPorEmail(email, SecurityUtil.sha256(novaSenha));
        }

        HttpUtil.text(ex,200,"Senha redefinida com sucesso! Faca login com a nova senha.");
    }

    public static void login(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String body = HttpUtil.body(ex);
        String email = JsonUtil.str(body,"email");
        String senha = JsonUtil.str(body,"password");
        String tipo = JsonUtil.or(body,"tipo","usuario");

        if ("admin".equals(tipo)) {
            Admin admin = AdminDao.buscarPorEmail(email);
            if (admin == null || !admin.getPassword().equals(SecurityUtil.sha256(senha))) {
                HttpUtil.text(ex,401,"E-mail ou senha incorretos."); return;
            }
            HttpUtil.json(ex,200,String.format("{\"tipo\":\"admin\",\"id\":%d,\"username\":\"%s\"}",
                admin.getId(), JsonUtil.esc(admin.getUsername())));
            return;
        }

        Usuario usuario = UsuarioDao.buscarPorEmail(email);
        if (usuario == null || !usuario.getPassword().equals(SecurityUtil.sha256(senha))) {
            HttpUtil.text(ex,401,"E-mail ou senha incorretos."); return;
        }
        if (usuario.getVerificado() == 0) { HttpUtil.text(ex,403,"EMAIL_NAO_VERIFICADO:" + email + ":usuario"); return; }
        if (usuario.getAtivo() == 0) { HttpUtil.text(ex,403,"Conta desativada. Contate o administrador."); return; }

        LogDao.registrarUsuario(usuario.getId(),"LOGIN",null);
        HttpUtil.json(ex,200,String.format("{\"tipo\":\"usuario\",\"id\":%d,\"username\":\"%s\"}",
            usuario.getId(), JsonUtil.esc(usuario.getUsername())));
    }

    private static boolean tokenValido(String tokenSalvo, Timestamp expira, String token, HttpExchange ex) throws Exception {
        if (tokenSalvo == null || !tokenSalvo.equals(token)) {
            HttpUtil.text(ex,400,"Token invalido."); return false;
        }
        if (expira != null && expira.before(new Timestamp(System.currentTimeMillis()))) {
            HttpUtil.text(ex,400,"Token expirado. Solicite um novo link."); return false;
        }
        return true;
    }
}
