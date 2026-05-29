package dto.prompt;

import model.Prompt;

public class PromptResponse {
    public final int id;
    public final int usuarioId;
    public final int categoriaId;
    public final String categoria;
    public final String titulo;
    public final String conteudo;
    public final String descricao;
    public final boolean favorito;
    public final String criadoEm;
    public final String atualizadoEm;

    public PromptResponse(Prompt prompt) {
        this.id = prompt.getId();
        this.usuarioId = prompt.getUsuarioId();
        this.categoriaId = prompt.getCategoriaId();
        this.categoria = prompt.getCategoria();
        this.titulo = prompt.getTitulo();
        this.conteudo = prompt.getConteudo();
        this.descricao = prompt.getDescricao();
        this.favorito = prompt.isFavorito();
        this.criadoEm = prompt.getCriadoEm();
        this.atualizadoEm = prompt.getAtualizadoEm();
    }
}
