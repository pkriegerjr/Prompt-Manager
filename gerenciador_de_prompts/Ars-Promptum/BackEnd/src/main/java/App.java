import controller.AdminController;
import controller.AuthController;
import controller.CategoriaController;
import controller.PromptController;
import dao.Database;
import dao.UsuarioDao;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import java.nio.file.Path;
import java.sql.SQLException;

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

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));
            config.router.ignoreTrailingSlashes = true;
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = Path.of("../FrontEnd").toAbsolutePath().normalize().toString();
                staticFiles.location = Location.EXTERNAL;
            });

            config.routes.get("/", ctx -> ctx.redirect("/pages/index.html"));
            config.routes.post("/api/usuarios", route(AuthController::usuarios));
            config.routes.get("/api/verificar", route(AuthController::verificar));
            config.routes.post("/api/reenviar", route(AuthController::reenviar));
            config.routes.post("/api/esqueci-senha", route(AuthController::esqueciSenha));
            config.routes.post("/api/redefinir-senha", route(AuthController::redefinirSenha));
            config.routes.post("/api/login", route(AuthController::login));

            config.routes.get("/api/prompts", route(PromptController::listarPorUsuario));
            config.routes.get("/api/prompts/{id}", route(PromptController::buscarPorId));
            config.routes.post("/api/prompts", route(PromptController::criar));
            config.routes.put("/api/prompts/{id}", route(PromptController::atualizar));
            config.routes.delete("/api/prompts/{id}", route(PromptController::deletar));

            config.routes.get("/api/categorias", route(CategoriaController::categorias));

            config.routes.get("/api/admin/stats", route(AdminController::stats));
            config.routes.get("/api/admin/usuarios", route(AdminController::usuarios));
            config.routes.post("/api/admin/usuarios/{id}/ativar", route(AdminController::ativarUsuario));
            config.routes.post("/api/admin/usuarios/{id}/desativar", route(AdminController::desativarUsuario));
            config.routes.post("/api/admin/tornar-admin", route(AdminController::tornarAdmin));
            config.routes.post("/api/admin/revogar-admin", route(AdminController::revogarAdmin));
            config.routes.delete("/api/admin/deletar-usuario/{id}", route(AdminController::deletarUsuario));
            config.routes.get("/api/admin/prompts", route(AdminController::prompts));
            config.routes.put("/api/admin/prompts/{id}", route(AdminController::atualizarPrompt));
            config.routes.delete("/api/admin/prompts/{id}", route(AdminController::deletarPrompt));
            config.routes.get("/api/admin/categorias", route(CategoriaController::adminCategorias));
            config.routes.post("/api/admin/categorias", route(CategoriaController::criarAdminCategoria));
            config.routes.put("/api/admin/categorias/{id}", route(CategoriaController::atualizarAdminCategoria));
            config.routes.delete("/api/admin/categorias/{id}", route(CategoriaController::deletarAdminCategoria));
            config.routes.get("/api/admin/logs", route(AdminController::logs));
            config.routes.post("/api/admin/criar-admin", route(AdminController::criarAdmin));
        }).start(8081);

        System.out.println("========================================");
        System.out.println("  Ars Prompt rodando em localhost:8081");
        System.out.println("========================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            Database.close();
        }));
        // Mantem o processo vivo quando executado como JAR local.
        new java.util.concurrent.CountDownLatch(1).await();
    }

    private static Handler route(Handler handler) {
        return ctx -> {
            try {
                handler.handle(ctx);
            } catch (Exception e) {
                System.err.println("[ERRO] " + e.getMessage());
                ctx.status(500).contentType("text/plain; charset=UTF-8").result("Erro interno: " + e.getMessage());
            }
        };
    }
}
