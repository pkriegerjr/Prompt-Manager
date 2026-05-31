package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.AdminStats;

public final class AdminStatsDao {
    private AdminStatsDao() {}

    public static AdminStats carregar() throws SQLException {
        int usuarios = count("SELECT COUNT(*) FROM usuarios");
        int ativos = count("SELECT COUNT(*) FROM usuarios WHERE ativo=1");
        int prompts = count("SELECT COUNT(*) FROM prompts");
        int categorias = count("SELECT COUNT(*) FROM categorias");
        int logs = count("SELECT COUNT(*) FROM historico_logs");
        int[] chart = new int[7];

        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT DATE(feito_em) AS dia,COUNT(*) AS total FROM historico_logs WHERE feito_em>=DATE_SUB(CURDATE(),INTERVAL 6 DAY) GROUP BY dia ORDER BY dia");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long diff = (System.currentTimeMillis() - rs.getDate("dia").getTime()) / 86400000L;
                int pos = 6 - (int) diff;
                if (pos >= 0 && pos < 7) chart[pos] = rs.getInt("total");
            }
        }
        return new AdminStats(usuarios, ativos, prompts, categorias, logs, chart);
    }

    private static int count(String sql) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
