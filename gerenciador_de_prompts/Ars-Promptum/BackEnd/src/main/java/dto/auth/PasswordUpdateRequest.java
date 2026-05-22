package dto.auth;

public class PasswordUpdateRequest {
    public String email = "";
    public String token = "";
    public String novaSenha = "";
    public String tipo = "usuario";

    public String tipo() {
        return tipo == null || tipo.isBlank() ? "usuario" : tipo;
    }
}
