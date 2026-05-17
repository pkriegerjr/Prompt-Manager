package model;

import java.sql.Timestamp;

public class Admin {
    private final int id;
    private final String username;
    private final String email;
    private final String password;
    private final int verificado;
    private final String tokenVerificacao;
    private final Timestamp tokenExpira;
    private final String criadoEm;

    public Admin(int id, String username, String email, String password, int verificado,
                 String tokenVerificacao, Timestamp tokenExpira, String criadoEm) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.verificado = verificado;
        this.tokenVerificacao = tokenVerificacao;
        this.tokenExpira = tokenExpira;
        this.criadoEm = criadoEm;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getVerificado() { return verificado; }
    public String getTokenVerificacao() { return tokenVerificacao; }
    public Timestamp getTokenExpira() { return tokenExpira; }
    public String getCriadoEm() { return criadoEm; }
}
