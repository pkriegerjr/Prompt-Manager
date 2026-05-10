/**
 * Representa um usuário do sistema.
 */
public class Usuario {
    private int    id;
    private String username;
    private String email;
    private String password;   // hash BCrypt
    private int    ativo;      // 1 = ativo | 0 = desativado
    private String role;
    private String criadoEm;

    // ── Construtor completo (leitura do banco) ──
    public Usuario(int id, String username, String email, String password,
                   int ativo, String role, String criadoEm) {
        this.id        = id;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.ativo     = ativo;
        this.role      = role;
        this.criadoEm  = criadoEm;
    }

    // ── Construtor para cadastro (sem id ainda) ──
    public Usuario(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
        this.ativo    = 1;
        this.role     = "usuario";
    }

    // ── Getters ──
    public int    getId()        { return id; }
    public String getUsername()  { return username; }
    public String getEmail()     { return email; }
    public String getPassword()  { return password; }
    public int    getAtivo()     { return ativo; }
    public String getRole()      { return role; }
    public String getCriadoEm() { return criadoEm; }

    // ── Setters ──
    public void setAtivo(int ativo)    { this.ativo = ativo; }
    public void setPassword(String pw) { this.password = pw; }

    /** Converte o objeto para JSON simples (sem biblioteca externa) */
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"username\":\"%s\",\"email\":\"%s\",\"ativo\":%d,\"role\":\"%s\",\"criadoEm\":\"%s\"}",
            id, esc(username), esc(email), ativo, esc(role), esc(criadoEm)
        );
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
