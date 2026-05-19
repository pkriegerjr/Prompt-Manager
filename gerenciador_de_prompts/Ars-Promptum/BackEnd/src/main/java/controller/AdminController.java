package controller;

import com.sun.net.httpserver.HttpExchange;
import dao.AdminDao;
import dao.AdminStatsDao;
import dao.LogDao;
import dao.PromptDao;
import dao.UsuarioDao;
import java.sql.SQLIntegrityConstraintViolationException;
import model.AdminStats;
import model.LogEntry;
import model.Usuario;
import util.HttpUtil;
import util.JsonUtil;
import util.JsonViews;
import util.SecurityUtil;

public final class AdminController {
    private AdminController() {}

    public static void criarAdmin(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String body = HttpUtil.body(ex);
        String user = JsonUtil.str(body,"username");
        String email = JsonUtil.str(body,"email");
        String senha = JsonUtil.str(body,"password");

        try {
            SecurityUtil.validarEmail(email);
            SecurityUtil.validarSenha(senha);
            AdminDao.criar(user, email, SecurityUtil.sha256(senha));
        } catch (IllegalArgumentException e) {
            HttpUtil.text(ex,400,e.getMessage()); return;
        } catch (SQLIntegrityConstraintViolationException e) {
            HttpUtil.text(ex,400,"Email ou username ja cadastrado."); return;
        }

        // Sincroniza role na tabela usuarios, se existir conta com mesmo email
        Usuario usuarioExistente = UsuarioDao.buscarPorEmail(email);
        if (usuarioExistente != null) {
            UsuarioDao.setRole(usuarioExistente.getId(), "moderador");
        }

        LogDao.registrarAdmin(1,"ADMIN_CRIADO","Novo admin: " + user);
        HttpUtil.text(ex,200,"Administrador \"" + user + "\" criado com sucesso! Ja pode fazer login.");
    }

    public static void stats(HttpExchange ex) throws Exception {
        AdminStats stats = AdminStatsDao.carregar();
        HttpUtil.json(ex,200,JsonViews.stats(stats));
    }

    public static void usuarios(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path = ex.getRequestURI().getPath();
        if ("GET".equals(method)) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Usuario usuario : UsuarioDao.listarTodos()) {
                if (!first) sb.append(",");
                first = false;
                sb.append(JsonViews.usuarioAdmin(usuario));
            }
            HttpUtil.json(ex,200,sb.append("]").toString()); return;
        }
        if ("POST".equals(method)) {
            int ativo = path.endsWith("/ativar") ? 1 : 0;
            int id = HttpUtil.pathId(path.replace("/ativar","").replace("/desativar",""));
            UsuarioDao.setAtivo(id, ativo);
            LogDao.registrarAdmin(1, ativo == 1 ? "ATIVAR_CONTA" : "DESATIVAR_CONTA", "ID: " + id);
            HttpUtil.text(ex,200,ativo == 1 ? "Conta ativada." : "Conta desativada."); return;
        }
        HttpUtil.text(ex,405,"Metodo nao permitido");
    }

    public static void tornarAdmin(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String body = HttpUtil.body(ex);
        String idStr = JsonUtil.str(body,"id");
        if (idStr.isEmpty()) idStr = JsonUtil.num(body,"id");
        int id = Integer.parseInt(idStr);

        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ex,404,"Usuario nao encontrado."); return; }
        try {
            AdminDao.criarComSenhaHash(usuario.getUsername(), usuario.getEmail(), usuario.getPassword());
        } catch (SQLIntegrityConstraintViolationException e) {
            HttpUtil.text(ex,400,"Este usuario ja e administrador."); return;
        }

        UsuarioDao.setRole(id, "moderador");
        LogDao.registrarAdmin(1,"ADMIN_CRIADO","Promovido de usuario ID: " + id);
        HttpUtil.text(ex,200,"Usuario \"" + usuario.getUsername() + "\" promovido a administrador!");
    }

    public static void revogarAdmin(HttpExchange ex) throws Exception {
        if (!"POST".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        String body = HttpUtil.body(ex);
        String idStr = JsonUtil.str(body,"id");
        if (idStr.isEmpty()) idStr = JsonUtil.num(body,"id");
        int id = Integer.parseInt(idStr);

        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ex,404,"Usuario nao encontrado."); return; }

        boolean removido = AdminDao.removerPorEmail(usuario.getEmail());
        if (!removido) { HttpUtil.text(ex,400,"Este usuario nao e administrador."); return; }

        UsuarioDao.setRole(id, "usuario");
        LogDao.registrarAdmin(1,"ADMIN_REVOGADO","Privilégios removidos, usuario ID: " + id);
        HttpUtil.text(ex,200,"Privilegios de \"" + usuario.getUsername() + "\" removidos com sucesso!");
    }

    public static void deletarUsuario(HttpExchange ex) throws Exception {
        if (!"DELETE".equals(ex.getRequestMethod())) { HttpUtil.text(ex,405,"Metodo nao permitido"); return; }
        int id = HttpUtil.pathId(ex.getRequestURI().getPath());
        if (id <= 0) { HttpUtil.text(ex,400,"ID invalido."); return; }

        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ex,404,"Usuario nao encontrado."); return; }
        UsuarioDao.deletar(id);
        LogDao.registrarAdmin(1,"DESATIVAR_CONTA","Conta deletada: " + usuario.getUsername() + " (ID: " + id + ")");
        HttpUtil.text(ex,200,"Conta de \"" + usuario.getUsername() + "\" deletada com sucesso.");
    }

    public static void prompts(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path = ex.getRequestURI().getPath();
        if ("GET".equals(method)) {
            HttpUtil.json(ex,200,PromptController.promptArray(PromptDao.listarTodosAdmin())); return;
        }
        int id = HttpUtil.pathId(path);
        String body = HttpUtil.body(ex);
        if ("PUT".equals(method)) {
            int cat = JsonUtil.parseIntOrDefault(JsonUtil.or(body,"categoriaId","0"),0);
            if (cat <= 0) cat = JsonUtil.parseIntOrDefault(JsonUtil.num(body,"categoriaId"),0);
            PromptDao.atualizarAdmin(id, cat, JsonUtil.str(body,"titulo"), JsonUtil.str(body,"conteudo"));
            LogDao.registrarAdmin(1,"ADMIN_EDITAR_PROMPT","ID: " + id);
            HttpUtil.text(ex,200,"Prompt atualizado."); return;
        }
        if ("DELETE".equals(method)) {
            PromptDao.deletar(id);
            LogDao.registrarAdmin(1,"ADMIN_DELETAR_PROMPT","ID: " + id);
            HttpUtil.text(ex,200,"Prompt deletado."); return;
        }
        HttpUtil.text(ex,405,"Metodo nao permitido");
    }

    public static void logs(HttpExchange ex) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (LogEntry log : LogDao.listarRecentes()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(JsonViews.log(log));
        }
        HttpUtil.json(ex,200,sb.append("]").toString());
    }
}
