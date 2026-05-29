package model;

public class Prompt {
    private final int id;
    private final int usuarioId;
    private final int categoriaId;
    private final String categoria;
    private final String username;
    private final String titulo;
    private final String conteudo;
    private final String descricao;
    private final boolean favorito;
    private final String criadoEm;
    private final String atualizadoEm;

    public Prompt(int id, int usuarioId, int categoriaId, String categoria, String username,
                  String titulo, String conteudo, String descricao, boolean favorito, String criadoEm, String atualizadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.categoriaId = categoriaId;
        this.categoria = categoria;
        this.username = username;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.descricao = descricao;
        this.favorito = favorito;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public int getId() { return id; }
    public int getUsuarioId() { return usuarioId; }
    public int getCategoriaId() { return categoriaId; }
    public String getCategoria() { return categoria; }
    public String getUsername() { return username; }
    public String getTitulo() { return titulo; }
    public String getConteudo() { return conteudo; }
    public String getDescricao() { return descricao; }
    public boolean isFavorito() { return favorito; }
    public String getCriadoEm() { return criadoEm; }
    public String getAtualizadoEm() { return atualizadoEm; }
}
