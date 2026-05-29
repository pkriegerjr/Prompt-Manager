package controller;

import dao.AdminDao;
import dao.AdminStatsDao;
import dao.LogDao;
import dao.PromptDao;
import dao.UsuarioDao;
import dto.admin.AdminCreateRequest;
import dto.admin.AdminPromptUpdateRequest;
import dto.admin.AdminRoleRequest;
import dto.admin.AdminStatsResponse;
import dto.admin.AdminUsuarioResponse;
import dto.admin.LogResponse;
import dto.prompt.PromptResponse;
import io.javalin.http.Context;
import java.sql.SQLIntegrityConstraintViolationException;
import middleware.AdminAuthMiddleware;
import model.Usuario;
import util.HttpUtil;
import util.SecurityUtil;

public final class AdminController {
    private AdminController() {}

    public static void criarAdmin(Context ctx) throws Exception {
        AdminCreateRequest request = ctx.bodyAsClass(AdminCreateRequest.class);
        String user = value(request.username);
        String email = value(request.email);
        String senha = value(request.password);

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

        LogDao.registrarAdmin(adminId(ctx),"ADMIN_CRIADO","Novo admin: " + user);
        HttpUtil.text(ctx,200,"Administrador \"" + user + "\" criado com sucesso! Ja pode fazer login.");
    }

    public static void stats(Context ctx) throws Exception {
        ctx.status(200).json(new AdminStatsResponse(AdminStatsDao.carregar()));
    }

    public static void usuarios(Context ctx) throws Exception {
        ctx.status(200).json(UsuarioDao.listarTodos().stream()
            .map(AdminUsuarioResponse::new)
            .toList());
    }

    public static void ativarUsuario(Context ctx) throws Exception {
        setAtivo(ctx, 1);
    }

    public static void desativarUsuario(Context ctx) throws Exception {
        setAtivo(ctx, 0);
    }

    public static void tornarAdmin(Context ctx) throws Exception {
        int id = requestId(ctx.bodyAsClass(AdminRoleRequest.class));

        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ctx,404,"Usuario nao encontrado."); return; }
        try {
            AdminDao.criarComSenhaHash(usuario.getUsername(), usuario.getEmail(), usuario.getPassword());
        } catch (SQLIntegrityConstraintViolationException e) {
            HttpUtil.text(ctx,400,"Este usuario ja e administrador."); return;
        }

        UsuarioDao.setRole(id, "moderador");
        LogDao.registrarAdmin(adminId(ctx),"ADMIN_CRIADO","Promovido de usuario ID: " + id);
        HttpUtil.text(ctx,200,"Usuario \"" + usuario.getUsername() + "\" promovido a administrador!");
    }

    public static void revogarAdmin(Context ctx) throws Exception {
        int id = requestId(ctx.bodyAsClass(AdminRoleRequest.class));

        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ctx,404,"Usuario nao encontrado."); return; }

        boolean removido = AdminDao.removerPorEmail(usuario.getEmail());
        if (!removido) { HttpUtil.text(ctx,400,"Este usuario nao e administrador."); return; }

        UsuarioDao.setRole(id, "usuario");
        LogDao.registrarAdmin(adminId(ctx),"ADMIN_REVOGADO","Privilegios removidos, usuario ID: " + id);
        HttpUtil.text(ctx,200,"Privilegios de \"" + usuario.getUsername() + "\" removidos com sucesso!");
    }

    public static void deletarUsuario(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Usuario usuario = UsuarioDao.buscarPorId(id);
        if (usuario == null) { HttpUtil.text(ctx,404,"Usuario nao encontrado."); return; }
        UsuarioDao.deletar(id);
        LogDao.registrarAdmin(adminId(ctx),"DESATIVAR_CONTA","Conta deletada: " + usuario.getUsername() + " (ID: " + id + ")");
        HttpUtil.text(ctx,200,"Conta de \"" + usuario.getUsername() + "\" deletada com sucesso.");
    }

    public static void prompts(Context ctx) throws Exception {
        ctx.status(200).json(PromptDao.listarTodosAdmin().stream()
            .map(PromptResponse::new)
            .toList());
    }

    public static void atualizarPrompt(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        AdminPromptUpdateRequest request = ctx.bodyAsClass(AdminPromptUpdateRequest.class);
        int cat = request.categoriaId == null ? 0 : request.categoriaId;
        PromptDao.atualizarAdmin(id, cat, value(request.titulo), value(request.conteudo));
        LogDao.registrarAdmin(adminId(ctx),"ADMIN_EDITAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt atualizado.");
    }

    public static void deletarPrompt(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        PromptDao.deletar(id);
        LogDao.registrarAdmin(adminId(ctx),"ADMIN_DELETAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt deletado.");
    }

    public static void logs(Context ctx) throws Exception {
        ctx.status(200).json(LogDao.listarRecentes().stream()
            .map(LogResponse::new)
            .toList());
    }

    private static void setAtivo(Context ctx, int ativo) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        UsuarioDao.setAtivo(id, ativo);
        LogDao.registrarAdmin(adminId(ctx), ativo == 1 ? "ATIVAR_CONTA" : "DESATIVAR_CONTA", "ID: " + id);
        HttpUtil.text(ctx,200,ativo == 1 ? "Conta ativada." : "Conta desativada.");
    }

    private static int adminId(Context ctx) {
        return AdminAuthMiddleware.currentAdminId(ctx);
    }

    private static int requestId(AdminRoleRequest request) {
        if (request.id == null || request.id <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        return request.id;
    }

    private static String value(String value) {
        return value == null ? "" : value.trim();
    }
}
