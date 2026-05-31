package controller;

import dao.CategoriaDao;
import dao.LogDao;
import dto.categoria.CategoriaRequest;
import dto.categoria.CategoriaResponse;
import io.javalin.http.Context;
import middleware.AdminAuthMiddleware;
import util.HttpUtil;

public final class CategoriaController {
    private CategoriaController() {}

    public static void categorias(Context ctx) throws Exception {
        ctx.status(200).json(CategoriaDao.listarTodas().stream()
            .map(CategoriaResponse::new)
            .toList());
    }

    public static void adminCategorias(Context ctx) throws Exception {
        categorias(ctx);
    }

    public static void criarAdminCategoria(Context ctx) throws Exception {
        CategoriaRequest request = ctx.bodyAsClass(CategoriaRequest.class);
        String nome = value(request.nome);
        String descricao = value(request.descricao);
        CategoriaDao.criar(nome, descricao);
        LogDao.registrarAdmin(adminId(ctx),"CRIAR_CATEGORIA",nome);
        HttpUtil.text(ctx,200,"Categoria criada.");
    }

    public static void atualizarAdminCategoria(Context ctx) throws Exception {
        CategoriaRequest request = ctx.bodyAsClass(CategoriaRequest.class);
        String nome = value(request.nome);
        String descricao = value(request.descricao);
        int id = Integer.parseInt(ctx.pathParam("id"));
        CategoriaDao.atualizar(id,nome,descricao);
        LogDao.registrarAdmin(adminId(ctx),"EDITAR_CATEGORIA","ID: " + id);
        HttpUtil.text(ctx,200,"Categoria atualizada.");
    }

    public static void deletarAdminCategoria(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        CategoriaDao.deletar(id);
        LogDao.registrarAdmin(adminId(ctx),"DELETAR_CATEGORIA","ID: " + id);
        HttpUtil.text(ctx,200,"Categoria deletada.");
    }

    private static int adminId(Context ctx) {
        return AdminAuthMiddleware.currentAdminId(ctx);
    }

    private static String value(String value) {
        return value == null ? "" : value.trim();
    }
}
