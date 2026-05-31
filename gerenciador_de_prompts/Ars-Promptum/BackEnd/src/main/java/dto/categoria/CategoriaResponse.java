package dto.categoria;

import model.Categoria;

public class CategoriaResponse {
    public final int id;
    public final String nome;
    public final String descricao;

    public CategoriaResponse(Categoria categoria) {
        this.id = categoria.getId();
        this.nome = categoria.getNome();
        this.descricao = categoria.getDescricao();
    }
}
