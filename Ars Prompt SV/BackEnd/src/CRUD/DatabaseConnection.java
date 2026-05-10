import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexão singleton com o MySQL.
 * Reutiliza a mesma conexão em todo o servidor.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/ars_database?useSSL=false&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";
    private static final String USER     = "root";
    private static final String PASSWORD = "";   // root sem senha no XAMPP

    private static Connection connection = null;

    // Construtor privado — ninguém instancia direto
    private DatabaseConnection() {}

    /**
     * Retorna a conexão ativa. Se estiver fechada ou nula, cria uma nova.
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Carrega o driver JDBC do MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Conexão com MySQL estabelecida.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado. Verifique se o mysql-connector-j.jar está na pasta lib/", e);
        }
        return connection;
    }

    /**
     * Fecha a conexão com segurança.
     */
    public static void fechar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Conexão encerrada.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao fechar conexão: " + e.getMessage());
        }
    }
}
