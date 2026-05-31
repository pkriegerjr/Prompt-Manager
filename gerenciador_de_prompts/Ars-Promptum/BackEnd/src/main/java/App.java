import dao.Database;
import dao.UsuarioDao;
import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.router.JavalinDefaultRoutingApi;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import middleware.AdminAuthMiddleware;
import middleware.UserAuthMiddleware;
import routes.AdminRoutes;
import routes.AuthRoutes;
import routes.CategoriaRoutes;
import routes.PromptRoutes;

public class App {
    private static final int PORT = 8081;

    public static void main(String[] args) throws Exception {
        initDatabase();

        Javalin app = createApp().start(PORT);

        System.out.println("========================================");
        System.out.println("  Ars Prompt rodando em localhost:" + PORT);
        System.out.println("========================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            Database.close();
        }));
        // Mantem o processo vivo quando executado como JAR local.
        new java.util.concurrent.CountDownLatch(1).await();
    }

    public static Javalin createApp() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));
            config.router.ignoreTrailingSlashes = true;
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = frontendDirectory().toString();
                staticFiles.location = Location.EXTERNAL;
            });

            registerExceptionHandlers(config.routes);
            registerRoutes(config.routes);
        });
        return app;
    }

    private static void initDatabase() {
        try {
            Database.getConnection();
            System.out.println("[DB] Banco ars_database conectado!");
            UsuarioDao.migrarSenhas();
            Database.migrarSchema();
        } catch (SQLException e) {
            System.err.println("[ERRO] Nao foi possivel conectar ao banco: " + e.getMessage());
            System.err.println(">>> Verifique se o MySQL esta rodando.");
            System.exit(1);
        }
    }

    private static Path frontendDirectory() {
        Path defaultPath = Path.of("../FrontEnd").toAbsolutePath().normalize();
        if (Files.exists(defaultPath.resolve("pages/index.html"))) return defaultPath;

        Path fromRepoRoot = Path.of("gerenciador_de_prompts/Ars-Promptum/FrontEnd").toAbsolutePath().normalize();
        if (Files.exists(fromRepoRoot.resolve("pages/index.html"))) return fromRepoRoot;

        Path fromAggregator = Path.of("Ars-Promptum/FrontEnd").toAbsolutePath().normalize();
        if (Files.exists(fromAggregator.resolve("pages/index.html"))) return fromAggregator;

        return defaultPath;
    }

    private static void registerRoutes(JavalinDefaultRoutingApi routes) {
        routes.get("/", ctx -> ctx.redirect("/pages/index.html"));
        AuthRoutes.register(routes);
        routes.before("/api/prompts", UserAuthMiddleware::requireUser);
        routes.before("/api/prompts/*", UserAuthMiddleware::requireUser);
        PromptRoutes.register(routes);
        routes.before("/api/admin/*", AdminAuthMiddleware::requireAdmin);
        CategoriaRoutes.register(routes);
        AdminRoutes.register(routes);
    }

    private static void registerExceptionHandlers(JavalinDefaultRoutingApi routes) {
        routes.exception(UnauthorizedResponse.class, (e, ctx) -> {
            System.err.println("[AUTH] " + e.getMessage());
            ctx.status(401).json(Map.of("erro", e.getMessage()));
        });

        routes.exception(IllegalArgumentException.class, (e, ctx) -> {
            System.err.println("[VALIDACAO] " + e.getMessage());
            ctx.status(400).json(Map.of("erro", e.getMessage()));
        });

        routes.exception(SQLException.class, (e, ctx) -> {
            System.err.println("[DB] " + e.getMessage());
            ctx.status(500).json(Map.of("erro", "Erro ao acessar o banco de dados."));
        });

        routes.exception(Exception.class, (e, ctx) -> {
            System.err.println("[ERRO] " + e.getMessage());
            ctx.status(500).json(Map.of("erro", "Erro interno: " + e.getMessage()));
        });
    }
}
