/**
 * Representa uma categoria de prompts.
 */
public class Categoria {
    private int    id;
    private String nome;
    private String descricao;

    public Categoria(int id, String nome, String descricao) {
        this.id       = id;
        this.nome     = nome;
        this.descricao = descricao;
    }

    public Categoria(String nome, String descricao) {
        this.nome     = nome;
        this.descricao = descricao;
    }

    public int    getId()        { return id; }
    public String getNome()      { return nome; }
    public String getDescricao() { return descricao; }
    public void   setNome(String nome)           { this.nome = nome; }
    public void   setDescricao(String descricao) { this.descricao = descricao; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"descricao\":\"%s\"}",
            id, esc(nome), esc(descricao)
        );
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
