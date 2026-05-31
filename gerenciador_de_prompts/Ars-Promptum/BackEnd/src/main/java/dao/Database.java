package dao;

import config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static Connection conn;

    private Database() {}

    public static Connection getConnection() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USER, AppConfig.DB_PASS);
                System.out.println("[DB] Conexao estabelecida.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver nao encontrado. Verifique o jar no -cp");
        }
        return conn;
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (Exception ignored) {}
    }

    public static void migrarSchema() {
        try {
            garantirColuna("prompts", "descricao", "VARCHAR(255) NULL", "conteudo");
            garantirColuna("admins", "token_verificacao", "VARCHAR(36) NULL", "verificado");
            garantirColuna("admins", "token_expira", "DATETIME NULL", "token_verificacao");
        } catch (Exception e) {
            System.err.println("[AVISO] Erro na migracao do schema: " + e.getMessage());
        }
    }

    private static void garantirColuna(String tabela, String coluna, String definicao, String depoisDe) throws SQLException {
        if (colunaExiste(tabela, coluna)) return;
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + definicao + " AFTER " + depoisDe);
        }
        System.out.println("[DB] Coluna adicionada: " + tabela + "." + coluna);
    }

    private static boolean colunaExiste(String tabela, String coluna) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            ps.setString(1, tabela);
            ps.setString(2, coluna);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
