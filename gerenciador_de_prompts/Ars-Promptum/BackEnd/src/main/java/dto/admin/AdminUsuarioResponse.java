package dto.admin;

import model.Usuario;

public class AdminUsuarioResponse {
    public final int id;
    public final String username;
    public final String email;
    public final int ativo;
    public final int verificado;
    public final String role;
    public final String criadoEm;

    public AdminUsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.ativo = usuario.getAtivo();
        this.verificado = usuario.getVerificado();
        this.role = usuario.getRole();
        this.criadoEm = usuario.getCriadoEm();
    }
}
