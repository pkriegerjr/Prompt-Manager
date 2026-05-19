package controller;

import com.sun.net.httpserver.HttpExchange;
import dao.CategoriaDao;
import dao.LogDao;
import model.Categoria;
import util.HttpUtil;
import util.JsonUtil;
import util.JsonViews;

public final class CategoriaController {
    private CategoriaController() {}

    public static void categorias(HttpExchange ex) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Categoria categoria : CategoriaDao.listarTodas()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(JsonViews.categoria(categoria));
        }
        HttpUtil.json(ex,200,sb.append("]").toString());
    }

    public static void adminCategorias(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path = ex.getRequestURI().getPath();
        String body = "GET".equals(method) ? "" : HttpUtil.body(ex);
        if ("GET".equals(method)) { categorias(ex); return; }
        if ("POST".equals(method)) {
            CategoriaDao.criar(JsonUtil.str(body,"nome"),JsonUtil.or(body,"descricao",""));
            LogDao.registrarAdmin(1,"CRIAR_CATEGORIA",JsonUtil.str(body,"nome"));
            HttpUtil.text(ex,200,"Categoria criada."); return;
        }
        int id = HttpUtil.pathId(path);
        if ("PUT".equals(method)) {
            CategoriaDao.atualizar(id,JsonUtil.str(body,"nome"),JsonUtil.or(body,"descricao",""));
            LogDao.registrarAdmin(1,"EDITAR_CATEGORIA","ID: " + id);
            HttpUtil.text(ex,200,"Categoria atualizada."); return;
        }
        if ("DELETE".equals(method)) {
            CategoriaDao.deletar(id);
            LogDao.registrarAdmin(1,"DELETAR_CATEGORIA","ID: " + id);
            HttpUtil.text(ex,200,"Categoria deletada."); return;
        }
        HttpUtil.text(ex,405,"Metodo nao permitido");
    }
}
