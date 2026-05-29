package controller;

import dao.CategoriaDao;
import dao.LogDao;
import dao.PromptDao;
import dto.prompt.PromptRequest;
import dto.prompt.PromptResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import middleware.UserAuthMiddleware;
import model.Prompt;
import util.HttpUtil;

public final class PromptController {
    private PromptController() {}

    public static void listarPorUsuario(Context ctx) throws Exception {
        int uid = parseIntOrDefault(ctx.queryParam("uid"), 0);
        if (uid <= 0) { HttpUtil.text(ctx,400,"Usuario invalido. Faca login novamente."); return; }
        validarUsuarioAutenticado(ctx, uid);
        ctx.status(200).json(PromptDao.listarPorUsuario(uid).stream()
            .map(PromptResponse::new)
            .toList());
    }

    public static void buscarPorId(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Prompt prompt = PromptDao.buscarPorId(id);
        if (prompt == null) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        validarDonoPrompt(ctx, prompt);
        ctx.status(200).json(new PromptResponse(prompt));
    }

    public static void criar(Context ctx) throws Exception {
        PromptRequest request = ctx.bodyAsClass(PromptRequest.class);
        int uid = UserAuthMiddleware.currentUserId(ctx);
        validarUsuarioRequest(ctx, usuarioId(request));
        if (uid <= 0) { HttpUtil.text(ctx,400,"Usuario invalido. Faca login novamente."); return; }
        if (value(request.titulo).isBlank() || value(request.conteudo).isBlank()) {
            HttpUtil.text(ctx,400,"Titulo e conteudo sao obrigatorios."); return;
        }
        int catId = categoriaId(request);
        PromptDao.criar(uid, catId, value(request.titulo), value(request.conteudo), value(request.descricao));
        LogDao.registrarUsuario(uid,"CRIAR_PROMPT","Titulo: " + value(request.titulo));
        HttpUtil.text(ctx,200,"Prompt criado com sucesso!");
    }

    public static void atualizar(Context ctx) throws Exception {
        PromptRequest request = ctx.bodyAsClass(PromptRequest.class);
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (value(request.titulo).isBlank() || value(request.conteudo).isBlank()) {
            HttpUtil.text(ctx,400,"Titulo e conteudo sao obrigatorios."); return;
        }
        if (buscarPromptDoUsuario(ctx, id) == null) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        boolean ok = PromptDao.atualizar(id, categoriaId(request), value(request.titulo), value(request.conteudo), value(request.descricao));
        if (!ok) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        LogDao.registrarUsuario(UserAuthMiddleware.currentUserId(ctx),"EDITAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt atualizado!");
    }

    public static void deletar(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (buscarPromptDoUsuario(ctx, id) == null) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        boolean ok = PromptDao.deletar(id);
        if (!ok) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        LogDao.registrarUsuario(UserAuthMiddleware.currentUserId(ctx),"DELETAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt deletado.");
    }

    public static void favoritar(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (buscarPromptDoUsuario(ctx, id) == null) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        boolean favorito = Boolean.parseBoolean(ctx.queryParam("valor"));
        boolean ok = PromptDao.setFavorito(id, favorito);
        if (!ok) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        HttpUtil.text(ctx,200, favorito ? "Adicionado aos favoritos." : "Removido dos favoritos.");
    }

    static int usuarioId(PromptRequest request) {
        return request.usuarioId == null ? 0 : request.usuarioId;
    }

    static int categoriaId(PromptRequest request) throws Exception {
        int id = request.categoriaId == null ? 0 : request.categoriaId;
        if (id <= 0) id = parseIntOrDefault(value(request.categoria), 0);
        if (id > 0) return CategoriaDao.existe(id) ? id : 0;
        String nome = value(request.categoria);
        return (nome.isEmpty() || "0".equals(nome)) ? 0 : CategoriaDao.buscarOuCriar(nome);
    }

    private static String value(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parseIntOrDefault(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static void validarUsuarioRequest(Context ctx, int requestUserId) {
        if (requestUserId > 0) validarUsuarioAutenticado(ctx, requestUserId);
    }

    private static void validarUsuarioAutenticado(Context ctx, int userId) {
        if (UserAuthMiddleware.currentUserId(ctx) != userId) {
            throw new UnauthorizedResponse("Sessao de usuario invalida.");
        }
    }

    private static Prompt buscarPromptDoUsuario(Context ctx, int promptId) throws Exception {
        Prompt prompt = PromptDao.buscarPorId(promptId);
        if (prompt == null) {
            return null;
        }
        validarDonoPrompt(ctx, prompt);
        return prompt;
    }

    private static void validarDonoPrompt(Context ctx, Prompt prompt) {
        validarUsuarioAutenticado(ctx, prompt.getUsuarioId());
    }
}
