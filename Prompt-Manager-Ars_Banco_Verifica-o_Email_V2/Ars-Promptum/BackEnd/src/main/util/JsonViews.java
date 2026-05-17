package util;

import model.AdminStats;
import model.Categoria;
import model.LogEntry;
import model.Prompt;
import model.Usuario;

public final class JsonViews {
    private JsonViews() {}

    public static String categoria(Categoria c) {
        return String.format("{\"id\":%d,\"nome\":\"%s\",\"descricao\":\"%s\"}",
            c.getId(), JsonUtil.esc(c.getNome()), JsonUtil.esc(c.getDescricao()));
    }

    public static String prompt(Prompt p) {
        return String.format("{\"id\":%d,\"usuarioId\":%d,\"categoriaId\":%d,\"categoria\":\"%s\",\"titulo\":\"%s\",\"conteudo\":\"%s\",\"descricao\":\"%s\",\"criadoEm\":\"%s\",\"atualizadoEm\":\"%s\"}",
            p.getId(), p.getUsuarioId(), p.getCategoriaId(),
            JsonUtil.esc(p.getCategoria()),
            JsonUtil.esc(p.getTitulo()), JsonUtil.esc(p.getConteudo()),
            JsonUtil.esc(p.getDescricao()),
            JsonUtil.esc(p.getCriadoEm()), JsonUtil.esc(p.getAtualizadoEm()));
    }

    public static String usuarioAdmin(Usuario u) {
        return String.format("{\"id\":%d,\"username\":\"%s\",\"email\":\"%s\",\"ativo\":%d,\"verificado\":%d,\"role\":\"%s\",\"criadoEm\":\"%s\"}",
            u.getId(), JsonUtil.esc(u.getUsername()), JsonUtil.esc(u.getEmail()),
            u.getAtivo(), u.getVerificado(), JsonUtil.esc(u.getRole()), JsonUtil.esc(u.getCriadoEm()));
    }

    public static String log(LogEntry l) {
        return String.format("{\"id\":%d,\"acao\":\"%s\",\"usuario\":\"%s\",\"detalhes\":\"%s\",\"feito_em\":\"%s\"}",
            l.getId(), JsonUtil.esc(l.getAcao()), JsonUtil.esc(l.getUsuario()),
            JsonUtil.esc(l.getDetalhes()), JsonUtil.esc(l.getFeitoEm()));
    }

    public static String stats(AdminStats stats) {
        int[] chart = stats.getChart();
        return String.format("{\"usuarios\":%d,\"usuariosAtivos\":%d,\"prompts\":%d,\"categorias\":%d,\"logs\":%d,\"chart\":[%d,%d,%d,%d,%d,%d,%d]}",
            stats.getUsuarios(), stats.getUsuariosAtivos(), stats.getPrompts(), stats.getCategorias(), stats.getLogs(),
            chart[0], chart[1], chart[2], chart[3], chart[4], chart[5], chart[6]);
    }
}
