package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import model.Usuario;
import util.SecurityUtil;

public final class UsuarioDao {
    private UsuarioDao() {}

    public static void criar(String username, String email, String passwordHash, String token)
            throws SQLException, SQLIntegrityConstraintViolationException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO usuarios (username, email, password, verificado, token_verificacao, token_expira) " +
                "VALUES (?,?,?,0,?,DATE_ADD(NOW(), INTERVAL 24 HOUR))")) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, token);
            ps.executeUpdate();
        }
    }

    public static Usuario buscarPorTokenVerificacao(String token) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT * FROM usuarios WHERE token_verificacao=?")) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public static Usuario buscarPorEmail(String email) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM usuarios WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public static Usuario buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM usuarios WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public static List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM usuarios ORDER BY criado_em DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) usuarios.add(map(rs));
        }
        return usuarios;
    }

    public static void verificarEmail(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE usuarios SET verificado=1, ativo=1, token_verificacao=NULL, token_expira=NULL WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static void atualizarTokenVerificacao(String email, String token) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE usuarios SET token_verificacao=?, token_expira=DATE_ADD(NOW(), INTERVAL 24 HOUR) WHERE email=?")) {
            ps.setString(1, token);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public static void atualizarTokenReset(String email, String token) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE usuarios SET token_verificacao=?, token_expira=DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email=?")) {
            ps.setString(1, token);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public static void atualizarSenhaPorEmail(String email, String passwordHash) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE usuarios SET password=?, token_verificacao=NULL, token_expira=NULL WHERE email=?")) {
            ps.setString(1, passwordHash);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public static void setAtivo(int id, int ativo) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("UPDATE usuarios SET ativo=? WHERE id=?")) {
            ps.setInt(1, ativo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public static void deletar(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM usuarios WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static void migrarSenhas() {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT id, password FROM usuarios");
             ResultSet rs = ps.executeQuery()) {
            int migradas = 0;
            while (rs.next()) {
                String password = rs.getString("password");
                if (password != null && password.length() < 60) {
                    try (PreparedStatement up = Database.getConnection().prepareStatement("UPDATE usuarios SET password=? WHERE id=?")) {
                        up.setString(1, SecurityUtil.sha256(password));
                        up.setInt(2, rs.getInt("id"));
                        up.executeUpdate();
                        migradas++;
                    }
                }
            }
            if (migradas > 0) System.out.println("[DB] " + migradas + " senha(s) migrada(s) para SHA-256.");
        } catch (Exception e) {
            System.err.println("[AVISO] Erro na migracao: " + e.getMessage());
        }
    }
    public static void setRole(int id, String role) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("UPDATE usuarios SET role=? WHERE id=?")) {
            ps.setString(1, role);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private static Usuario map(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("ativo"),
            rs.getInt("verificado"),
            rs.getString("token_verificacao"),
            rs.getTimestamp("token_expira"),
            rs.getString("role"),
            String.valueOf(rs.getTimestamp("criado_em"))
        );
    }
}
