package dto.auth;

public class LoginResponse {
    public final String tipo;
    public final int id;
    public final String username;
    public final String token;

    public LoginResponse(String tipo, int id, String username, String token) {
        this.tipo = tipo;
        this.id = id;
        this.username = username;
        this.token = token;
    }
}
