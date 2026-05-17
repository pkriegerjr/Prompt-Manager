import com.sun.net.httpserver.HttpServer;
import controller.AdminController;
import controller.AuthController;
import controller.CategoriaController;
import controller.PromptController;
import dao.Database;
import dao.UsuarioDao;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import util.HttpUtil;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            Database.getConnection();
            System.out.println("[DB] Banco ars_database conectado!");
            UsuarioDao.migrarSenhas();
            Database.migrarSchema();
        } catch (SQLException e) {
            System.err.println("[ERRO] Nao foi possivel conectar ao banco: " + e.getMessage());
            System.err.println(">>> Verifique se o MySQL do XAMPP esta rodando.");
            System.exit(1);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        server.createContext("/api/usuarios", ex -> HttpUtil.route(ex, AuthController::usuarios));
        server.createContext("/api/verificar", ex -> HttpUtil.route(ex, AuthController::verificar));
        server.createContext("/api/reenviar", ex -> HttpUtil.route(ex, AuthController::reenviar));
        server.createContext("/api/esqueci-senha", ex -> HttpUtil.route(ex, AuthController::esqueciSenha));
        server.createContext("/api/redefinir-senha", ex -> HttpUtil.route(ex, AuthController::redefinirSenha));
        server.createContext("/api/login", ex -> HttpUtil.route(ex, AuthController::login));
        server.createContext("/api/prompts", ex -> HttpUtil.route(ex, PromptController::prompts));
        server.createContext("/api/categorias", ex -> HttpUtil.route(ex, CategoriaController::categorias));

        server.createContext("/api/admin/stats", ex -> HttpUtil.route(ex, AdminController::stats));
        server.createContext("/api/admin/usuarios", ex -> HttpUtil.route(ex, AdminController::usuarios));
        server.createContext("/api/admin/tornar-admin", ex -> HttpUtil.route(ex, AdminController::tornarAdmin));
        server.createContext("/api/admin/deletar-usuario", ex -> HttpUtil.route(ex, AdminController::deletarUsuario));
        server.createContext("/api/admin/prompts", ex -> HttpUtil.route(ex, AdminController::prompts));
        server.createContext("/api/admin/categorias", ex -> HttpUtil.route(ex, CategoriaController::adminCategorias));
        server.createContext("/api/admin/logs", ex -> HttpUtil.route(ex, AdminController::logs));
        server.createContext("/api/admin/criar-admin", ex -> HttpUtil.route(ex, AdminController::criarAdmin));

        System.out.println("========================================");
        System.out.println("  Ars Prompt rodando em localhost:8081");
        System.out.println("========================================");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(Database::close));
    }
}
