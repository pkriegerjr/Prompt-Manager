package controller;

import dao.AdminDao;
import dao.AdminStatsDao;
import dao.LogDao;
import dao.PromptDao;
import dao.UsuarioDao;
import io.javalin.http.Context;
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

    public static void criarAdmin(Context ctx) throws Exception {
        String body = HttpUtil.body(ctx);
        String user = JsonUtil.str(body,"username");
        String email = JsonUtil.str(body,"email");
        String senha = JsonUtil.str(body,"password");

        try {
            SecurityUtil.validarEmail(email);
            SecurityUtil.validarSenha(senha);
            AdminDao.criar(user, email, SecurityUtil.sha256(senha));
        } catch (IllegalArgumentException e) {
            HttpUtil.text(ctx,400,e.getMessage()); return;
        } catch (SQLIntegrityConstraintViolationException e) {
            HttpUtil.text(ctx,400,"Email ou username ja cadastrado."); return;
        }

        Usuario usuarioExistente = UsuarioDao.buscarPorEmail(email);
        if (usuarioExistente != null) {
            UsuarioDao.setRole(usuarioExistente.getId(), "moderador");
        }

        LogDao.registrarAdmin(1,"ADMIN_CRIADO","Novo admin: " + user);
        HttpUtil.text(ctx,200,"Administrador \"" + user + "\" criado com sucesso! Ja pode fazer login.");
    }

    public static void stats(Context ctx) throws Exception {
        AdminStats stats = AdminStatsDao.carregar();
        HttpUtil.json(ctx,200,JsonViews.stats(stats));
    }

    public static void usuarios(Context ctx) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Usuario usuario : UsuarioDao.listarTodos()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(JsonViews.usuarioAdmin(usuario));
        }
        HttpUtil.json(ctx,200,sb.append("]").toString());
    }

    public static void ativarUsuario(Context ctx) throws Exception {
        setAtivo(ctx, 1);
    }

    public static void desativarUsuario(Context ctx) throws Exception {
        setAtivo(ctx, 0);
    }

    public static void tornarAdmin(Context ctx) throws Exception {
        String body = HttpUtil.body(ctx);
        String idStr = JsonUtil.str(body,"id");
        if (idStr.isEmpty()) idStr = JsonUtil.num(body,"id");
        int id = Integer.parseInt(idStr);

        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ctx,404,"Usuario nao encontrado."); return; }
        try {
            AdminDao.criarComSenhaHash(usuario.getUsername(), usuario.getEmail(), usuario.getPassword());
        } catch (SQLIntegrityConstraintViolationException e) {
            HttpUtil.text(ctx,400,"Este usuario ja e administrador."); return;
        }

        UsuarioDao.setRole(id, "moderador");
        LogDao.registrarAdmin(1,"ADMIN_CRIADO","Promovido de usuario ID: " + id);
        HttpUtil.text(ctx,200,"Usuario \"" + usuario.getUsername() + "\" promovido a administrador!");
    }

    public static void revogarAdmin(Context ctx) throws Exception {
        String body = HttpUtil.body(ctx);
        String idStr = JsonUtil.str(body,"id");
        if (idStr.isEmpty()) idStr = JsonUtil.num(body,"id");
        int id = Integer.parseInt(idStr);

        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ctx,404,"Usuario nao encontrado."); return; }

        boolean removido = AdminDao.removerPorEmail(usuario.getEmail());
        if (!removido) { HttpUtil.text(ctx,400,"Este usuario nao e administrador."); return; }

        UsuarioDao.setRole(id, "usuario");
        LogDao.registrarAdmin(1,"ADMIN_REVOGADO","Privilegios removidos, usuario ID: " + id);
        HttpUtil.text(ctx,200,"Privilegios de \"" + usuario.getUsername() + "\" removidos com sucesso!");
    }

    public static void deletarUsuario(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ctx,404,"Usuario nao encontrado."); return; }
        UsuarioDao.deletar(id);
        LogDao.registrarAdmin(1,"DESATIVAR_CONTA","Conta deletada: " + usuario.getUsername() + " (ID: " + id + ")");
        HttpUtil.text(ctx,200,"Conta de \"" + usuario.getUsername() + "\" deletada com sucesso.");
    }

    public static void prompts(Context ctx) throws Exception {
        HttpUtil.json(ctx,200,PromptController.promptArray(PromptDao.listarTodosAdmin()));
    }

    public static void atualizarPrompt(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String body = HttpUtil.body(ctx);
        int cat = JsonUtil.parseIntOrDefault(JsonUtil.or(body,"categoriaId","0"),0);
        if (cat <= 0) cat = JsonUtil.parseIntOrDefault(JsonUtil.num(body,"categoriaId"),0);
        PromptDao.atualizarAdmin(id, cat, JsonUtil.str(body,"titulo"), JsonUtil.str(body,"conteudo"));
        LogDao.registrarAdmin(1,"ADMIN_EDITAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt atualizado.");
    }

    public static void deletarPrompt(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        PromptDao.deletar(id);
        LogDao.registrarAdmin(1,"ADMIN_DELETAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt deletado.");
    }

    public static void logs(Context ctx) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (LogEntry log : LogDao.listarRecentes()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(JsonViews.log(log));
        }
        HttpUtil.json(ctx,200,sb.append("]").toString());
    }

    private static void setAtivo(Context ctx, int ativo) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        UsuarioDao.setAtivo(id, ativo);
        LogDao.registrarAdmin(1, ativo == 1 ? "ATIVAR_CONTA" : "DESATIVAR_CONTA", "ID: " + id);
        HttpUtil.text(ctx,200,ativo == 1 ? "Conta ativada." : "Conta desativada.");
    }
}
