import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a tabela `historico_logs`.
 */
public class LogDAO {

    /**
     * Registra uma ação de usuário comum.
     */
    public void registrar(int usuarioId, String acao, String detalhes) {
        String sql = "INSERT INTO historico_logs (usuario_id, acao, detalhes) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, acao);
            ps.setString(3, detalhes);
            ps.executeUpdate();

        } catch (SQLException e) {
            // Log não deve travar o fluxo principal
            System.err.println("[LOG] Erro ao registrar log: " + e.getMessage());
        }
    }

    /**
     * Registra uma ação de administrador.
     */
    public void registrarAdmin(int adminId, String acao, String detalhes) {
        String sql = "INSERT INTO historico_logs (admin_id, acao, detalhes) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setString(2, acao);
            ps.setString(3, detalhes);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[LOG] Erro ao registrar log admin: " + e.getMessage());
        }
    }

    /**
     * Lista todos os logs (admin) — retorna JSON array.
     */
    public List<String> listarTodos() throws SQLException {
        String sql = "SELECT l.*, u.username AS nome_usuario " +
                     "FROM historico_logs l " +
                     "LEFT JOIN usuarios u ON l.usuario_id = u.id " +
                     "ORDER BY l.feito_em DESC LIMIT 200";
        List<String> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String usuario = rs.getString("nome_usuario");
                lista.add(String.format(
                    "{\"id\":%d,\"acao\":\"%s\",\"usuario\":\"%s\",\"detalhes\":\"%s\",\"feito_em\":\"%s\"}",
                    rs.getInt("id"),
                    esc(rs.getString("acao")),
                    esc(usuario == null ? "admin" : usuario),
                    esc(rs.getString("detalhes")),
                    esc(rs.getString("feito_em"))
                ));
            }
        }
        return lista;
    }

    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM historico_logs";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Atividade dos últimos 7 dias (para o gráfico do dashboard).
     * Retorna array JSON com 7 inteiros, do mais antigo ao mais recente.
     */
    public int[] atividadeSeteDias() throws SQLException {
        String sql = "SELECT DATE(feito_em) AS dia, COUNT(*) AS total " +
                     "FROM historico_logs " +
                     "WHERE feito_em >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                     "GROUP BY dia ORDER BY dia";
        int[] dados = new int[7];

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // posição 0 = 6 dias atrás, posição 6 = hoje
                Date dia   = rs.getDate("dia");
                long diff  = (System.currentTimeMillis() - dia.getTime()) / (1000 * 60 * 60 * 24);
                int  pos   = 6 - (int) diff;
                if (pos >= 0 && pos < 7) dados[pos] = rs.getInt("total");
            }
        }
        return dados;
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
