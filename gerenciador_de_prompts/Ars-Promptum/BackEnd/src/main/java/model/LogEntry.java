package model;

public class LogEntry {
    private final int id;
    private final String acao;
    private final String usuario;
    private final String detalhes;
    private final String feitoEm;

    public LogEntry(int id, String acao, String usuario, String detalhes, String feitoEm) {
        this.id = id;
        this.acao = acao;
        this.usuario = usuario;
        this.detalhes = detalhes;
        this.feitoEm = feitoEm;
    }

    public int getId() { return id; }
    public String getAcao() { return acao; }
    public String getUsuario() { return usuario; }
    public String getDetalhes() { return detalhes; }
    public String getFeitoEm() { return feitoEm; }
}
