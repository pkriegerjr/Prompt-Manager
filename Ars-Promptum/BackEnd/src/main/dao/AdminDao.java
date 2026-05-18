package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import model.Admin;

public final class AdminDao {
    private AdminDao() {}

    public static Admin buscarPorEmail(String email) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM admins WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public static boolean removerPorEmail(String email) throws SQLException {
    try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM admins WHERE email=?")) {
            ps.setString(1, email);
            return ps.executeUpdate() > 0;
        }
    }

    public static void criar(String username, String email, String passwordHash)
            throws SQLException, SQLIntegrityConstraintViolationException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO admins (username, email, password, verificado) VALUES (?,?,?,1)")) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
        }
    }

    public static void criarComSenhaHash(String username, String email, String passwordHash)
            throws SQLException, SQLIntegrityConstraintViolationException {
        criar(username, email, passwordHash);
    }

    public static void atualizarTokenReset(String email, String token) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE admins SET token_verificacao=?, token_expira=DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email=?")) {
            ps.setString(1, token);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public static void atualizarSenhaPorEmail(String email, String passwordHash) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE admins SET password=?, token_verificacao=NULL, token_expira=NULL WHERE email=?")) {
            ps.setString(1, passwordHash);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    private static Admin map(ResultSet rs) throws SQLException {
        return new Admin(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("verificado"),
            rs.getString("token_verificacao"),
            rs.getTimestamp("token_expira"),
            String.valueOf(rs.getTimestamp("criado_em"))
        );
    }
}
