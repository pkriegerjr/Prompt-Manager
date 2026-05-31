package model;

import java.sql.Timestamp;

public class Usuario {
    private final int id;
    private final String username;
    private final String email;
    private final String password;
    private final int ativo;
    private final int verificado;
    private final String tokenVerificacao;
    private final Timestamp tokenExpira;
    private final String role;
    private final String criadoEm;

    public Usuario(int id, String username, String email, String password, int ativo, int verificado,
                   String tokenVerificacao, Timestamp tokenExpira, String role, String criadoEm) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.ativo = ativo;
        this.verificado = verificado;
        this.tokenVerificacao = tokenVerificacao;
        this.tokenExpira = tokenExpira;
        this.role = role;
        this.criadoEm = criadoEm;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getAtivo() { return ativo; }
    public int getVerificado() { return verificado; }
    public String getTokenVerificacao() { return tokenVerificacao; }
    public Timestamp getTokenExpira() { return tokenExpira; }
    public String getRole() { return role; }
    public String getCriadoEm() { return criadoEm; }
}
