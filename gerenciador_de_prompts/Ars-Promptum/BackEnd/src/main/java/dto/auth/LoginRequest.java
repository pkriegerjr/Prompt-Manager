package dto.auth;

public class LoginRequest {
    public String email = "";
    public String password = "";
    public String tipo = "usuario";

    public String tipo() {
        return tipo == null || tipo.isBlank() ? "usuario" : tipo;
    }
}
