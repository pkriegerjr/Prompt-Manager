/**
 * Representa um prompt do sistema.
 */
public class Prompt {
    private int    id;
    private int    usuarioId;
    private int    categoriaId;   // 0 = sem categoria
    private String titulo;
    private String conteudo;
    private String criadoEm;
    private String atualizadoEm;

    // ── Construtor completo (leitura do banco) ──
    public Prompt(int id, int usuarioId, int categoriaId,
                  String titulo, String conteudo,
                  String criadoEm, String atualizadoEm) {
        this.id           = id;
        this.usuarioId    = usuarioId;
        this.categoriaId  = categoriaId;
        this.titulo       = titulo;
        this.conteudo     = conteudo;
        this.criadoEm     = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    // ── Construtor para criação ──
    public Prompt(int usuarioId, int categoriaId, String titulo, String conteudo) {
        this.usuarioId   = usuarioId;
        this.categoriaId = categoriaId;
        this.titulo      = titulo;
        this.conteudo    = conteudo;
    }

    // ── Getters ──
    public int    getId()           { return id; }
    public int    getUsuarioId()    { return usuarioId; }
    public int    getCategoriaId()  { return categoriaId; }
    public String getTitulo()       { return titulo; }
    public String getConteudo()     { return conteudo; }
    public String getCriadoEm()     { return criadoEm; }
    public String getAtualizadoEm() { return atualizadoEm; }

    // ── Setters ──
    public void setTitulo(String titulo)       { this.titulo = titulo; }
    public void setConteudo(String conteudo)   { this.conteudo = conteudo; }
    public void setCategoriaId(int catId)      { this.categoriaId = catId; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"usuarioId\":%d,\"categoriaId\":%d,\"titulo\":\"%s\",\"conteudo\":\"%s\",\"criadoEm\":\"%s\",\"atualizadoEm\":\"%s\"}",
            id, usuarioId, categoriaId,
            esc(titulo), esc(conteudo), esc(criadoEm), esc(atualizadoEm)
        );
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
