package controller;

import com.sun.net.httpserver.HttpExchange;
import dao.CategoriaDao;
import dao.LogDao;
import dao.PromptDao;
import java.util.List;
import model.Prompt;
import util.HttpUtil;
import util.JsonUtil;
import util.JsonViews;

public final class PromptController {
    private PromptController() {}

    public static void prompts(HttpExchange ex) throws Exception {
        String method = ex.getRequestMethod().toUpperCase();
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        if ("GET".equals(method)) {
            int id = HttpUtil.pathId(path);
            if (id > 0) {
                Prompt prompt = PromptDao.buscarPorId(id);
                if (prompt == null) { HttpUtil.text(ex,404,"Prompt nao encontrado."); return; }
                HttpUtil.json(ex,200,JsonViews.prompt(prompt)); return;
            }

            int uid = JsonUtil.queryInt(query, "uid");
            if (uid <= 0) { HttpUtil.text(ex,400,"Usuario invalido."); return; }
            HttpUtil.json(ex,200,promptArray(PromptDao.listarPorUsuario(uid))); return;
        }

        String body = HttpUtil.body(ex);
        if ("POST".equals(method)) {
            int uid = JsonUtil.parseIntOrDefault(JsonUtil.str(body,"usuarioId"),0);
            int catId = categoriaId(body);
            if (uid <= 0) { HttpUtil.text(ex,400,"Usuario invalido."); return; }
            if (JsonUtil.str(body,"titulo").isBlank() || JsonUtil.str(body,"conteudo").isBlank()) {
                HttpUtil.text(ex,400,"Titulo e conteudo sao obrigatorios."); return;
            }
            PromptDao.criar(uid, catId, JsonUtil.str(body,"titulo"), JsonUtil.str(body,"conteudo"), JsonUtil.or(body,"descricao",""));
            LogDao.registrarUsuario(uid,"CRIAR_PROMPT","Titulo: " + JsonUtil.str(body,"titulo"));
            HttpUtil.text(ex,200,"Prompt criado com sucesso!"); return;
        }

        int id = HttpUtil.pathId(path);
        if (id <= 0) { HttpUtil.text(ex,400,"ID invalido."); return; }
        if ("PUT".equals(method)) {
            if (JsonUtil.str(body,"titulo").isBlank() || JsonUtil.str(body,"conteudo").isBlank()) {
                HttpUtil.text(ex,400,"Titulo e conteudo sao obrigatorios."); return;
            }
            boolean ok = PromptDao.atualizar(id, categoriaId(body), JsonUtil.str(body,"titulo"), JsonUtil.str(body,"conteudo"), JsonUtil.or(body,"descricao",""));
            if (!ok) { HttpUtil.text(ex,404,"Prompt nao encontrado."); return; }
            LogDao.registrarUsuario(0,"EDITAR_PROMPT","ID: " + id);
            HttpUtil.text(ex,200,"Prompt atualizado!"); return;
        }
        if ("DELETE".equals(method)) {
            boolean ok = PromptDao.deletar(id);
            if (!ok) { HttpUtil.text(ex,404,"Prompt nao encontrado."); return; }
            LogDao.registrarUsuario(0,"DELETAR_PROMPT","ID: " + id);
            HttpUtil.text(ex,200,"Prompt deletado."); return;
        }
        HttpUtil.text(ex,405,"Metodo nao permitido");
    }

    static String promptArray(List<Prompt> prompts) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Prompt prompt : prompts) {
            if (!first) sb.append(",");
            first = false;
            sb.append(JsonViews.prompt(prompt));
        }
        return sb.append("]").toString();
    }

    static int categoriaId(String body) throws Exception {
        int id = JsonUtil.parseIntOrDefault(JsonUtil.str(body,"categoriaId"),0);
        if (id <= 0) id = JsonUtil.parseIntOrDefault(JsonUtil.num(body,"categoriaId"),0);
        if (id <= 0) id = JsonUtil.parseIntOrDefault(JsonUtil.str(body,"categoria").trim(),0);
        if (id <= 0) id = JsonUtil.parseIntOrDefault(JsonUtil.num(body,"categoria"),0);
        if (id > 0) return CategoriaDao.existe(id) ? id : 0;
        String nome = JsonUtil.str(body,"categoria").trim();
        return (nome.isEmpty() || "0".equals(nome)) ? 0 : CategoriaDao.buscarOuCriar(nome);
    }
}
