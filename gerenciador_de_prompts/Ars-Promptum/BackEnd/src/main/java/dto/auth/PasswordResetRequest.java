package dto.auth;

public class PasswordResetRequest {
    public String email = "";
    public String tipo = "usuario";

    public String tipo() {
        return tipo == null || tipo.isBlank() ? "usuario" : tipo;
    }
}
