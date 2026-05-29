package dto.admin;

import model.LogEntry;

public class LogResponse {
    public final int id;
    public final String acao;
    public final String usuario;
    public final String detalhes;
    public final String feito_em;

    public LogResponse(LogEntry log) {
        this.id = log.getId();
        this.acao = log.getAcao();
        this.usuario = log.getUsuario();
        this.detalhes = log.getDetalhes();
        this.feito_em = log.getFeitoEm();
    }
}
