package controller;

import dao.CategoriaDao;
import dao.LogDao;
import io.javalin.http.Context;
import model.Categoria;
import util.HttpUtil;
import util.JsonUtil;
import util.JsonViews;

public final class CategoriaController {
    private CategoriaController() {}

    public static void categorias(Context ctx) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Categoria categoria : CategoriaDao.listarTodas()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(JsonViews.categoria(categoria));
        }
        HttpUtil.json(ctx,200,sb.append("]").toString());
    }

    public static void adminCategorias(Context ctx) throws Exception {
        categorias(ctx);
    }

    public static void criarAdminCategoria(Context ctx) throws Exception {
        String body = HttpUtil.body(ctx);
        CategoriaDao.criar(JsonUtil.str(body,"nome"),JsonUtil.or(body,"descricao",""));
        LogDao.registrarAdmin(1,"CRIAR_CATEGORIA",JsonUtil.str(body,"nome"));
        HttpUtil.text(ctx,200,"Categoria criada.");
    }

    public static void atualizarAdminCategoria(Context ctx) throws Exception {
        String body = HttpUtil.body(ctx);
        int id = Integer.parseInt(ctx.pathParam("id"));
        CategoriaDao.atualizar(id,JsonUtil.str(body,"nome"),JsonUtil.or(body,"descricao",""));
        LogDao.registrarAdmin(1,"EDITAR_CATEGORIA","ID: " + id);
        HttpUtil.text(ctx,200,"Categoria atualizada.");
    }

    public static void deletarAdminCategoria(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        CategoriaDao.deletar(id);
        LogDao.registrarAdmin(1,"DELETAR_CATEGORIA","ID: " + id);
        HttpUtil.text(ctx,200,"Categoria deletada.");
    }
}
