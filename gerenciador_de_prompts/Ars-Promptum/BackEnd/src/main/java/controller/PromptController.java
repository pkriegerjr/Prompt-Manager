package controller;

import dao.CategoriaDao;
import dao.LogDao;
import dao.PromptDao;
import io.javalin.http.Context;
import java.util.List;
import model.Prompt;
import util.HttpUtil;
import util.JsonUtil;
import util.JsonViews;

public final class PromptController {
    private PromptController() {}

    public static void listarPorUsuario(Context ctx) throws Exception {
        int uid = JsonUtil.parseIntOrDefault(ctx.queryParam("uid"), 0);
        if (uid <= 0) { HttpUtil.text(ctx,400,"Usuario invalido. Faca login novamente."); return; }
        HttpUtil.json(ctx,200,promptArray(PromptDao.listarPorUsuario(uid)));
    }

    public static void buscarPorId(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Prompt prompt = PromptDao.buscarPorId(id);
        if (prompt == null) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        HttpUtil.json(ctx,200,JsonViews.prompt(prompt));
    }

    public static void criar(Context ctx) throws Exception {
        String body = HttpUtil.body(ctx);
        int uid = usuarioId(body);
        int catId = categoriaId(body);
        if (uid <= 0) { HttpUtil.text(ctx,400,"Usuario invalido. Faca login novamente."); return; }
        if (JsonUtil.str(body,"titulo").isBlank() || JsonUtil.str(body,"conteudo").isBlank()) {
            HttpUtil.text(ctx,400,"Titulo e conteudo sao obrigatorios."); return;
        }
        PromptDao.criar(uid, catId, JsonUtil.str(body,"titulo"), JsonUtil.str(body,"conteudo"), JsonUtil.or(body,"descricao",""));
        LogDao.registrarUsuario(uid,"CRIAR_PROMPT","Titulo: " + JsonUtil.str(body,"titulo"));
        HttpUtil.text(ctx,200,"Prompt criado com sucesso!");
    }

    public static void atualizar(Context ctx) throws Exception {
        String body = HttpUtil.body(ctx);
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (JsonUtil.str(body,"titulo").isBlank() || JsonUtil.str(body,"conteudo").isBlank()) {
            HttpUtil.text(ctx,400,"Titulo e conteudo sao obrigatorios."); return;
        }
        boolean ok = PromptDao.atualizar(id, categoriaId(body), JsonUtil.str(body,"titulo"), JsonUtil.str(body,"conteudo"), JsonUtil.or(body,"descricao",""));
        if (!ok) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        LogDao.registrarUsuario(0,"EDITAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt atualizado!");
    }

    public static void deletar(Context ctx) throws Exception {
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean ok = PromptDao.deletar(id);
        if (!ok) { HttpUtil.text(ctx,404,"Prompt nao encontrado."); return; }
        LogDao.registrarUsuario(0,"DELETAR_PROMPT","ID: " + id);
        HttpUtil.text(ctx,200,"Prompt deletado.");
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

    static int usuarioId(String body) {
        int id = JsonUtil.parseIntOrDefault(JsonUtil.str(body,"usuarioId"),0);
        if (id <= 0) id = JsonUtil.parseIntOrDefault(JsonUtil.num(body,"usuarioId"),0);
        return id;
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
