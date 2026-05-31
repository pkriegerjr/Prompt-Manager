package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.LogEntry;

public final class LogDao {
    private LogDao() {}

    public static void registrarUsuario(int usuarioId, String acao, String detalhes) {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO historico_logs (usuario_id,acao,detalhes) VALUES (?,?,?)")) {
            if (usuarioId > 0) ps.setInt(1, usuarioId); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, acao);
            ps.setString(3, detalhes);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[LOG] " + e.getMessage());
        }
    }

    public static void registrarAdmin(int adminId, String acao, String detalhes) {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO historico_logs (admin_id,acao,detalhes) VALUES (?,?,?)")) {
            ps.setInt(1, adminId);
            ps.setString(2, acao);
            ps.setString(3, detalhes);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[LOG] " + e.getMessage());
        }
    }

    public static List<LogEntry> listarRecentes() throws SQLException {
        List<LogEntry> logs = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT l.*,u.username FROM historico_logs l LEFT JOIN usuarios u ON l.usuario_id=u.id ORDER BY l.feito_em DESC LIMIT 200");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String user = rs.getString("username");
                logs.add(new LogEntry(
                    rs.getInt("id"),
                    rs.getString("acao"),
                    user == null ? "admin" : user,
                    rs.getString("detalhes"),
                    String.valueOf(rs.getTimestamp("feito_em"))
                ));
            }
        }
        return logs;
    }
}
