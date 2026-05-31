package routes;

import controller.AuthController;
import io.javalin.router.JavalinDefaultRoutingApi;

public final class AuthRoutes {
    private AuthRoutes() {}

    public static void register(JavalinDefaultRoutingApi routes) {
        routes.post("/api/usuarios", AuthController::usuarios);
        routes.get("/api/verificar", AuthController::verificar);
        routes.post("/api/reenviar", AuthController::reenviar);
        routes.post("/api/esqueci-senha", AuthController::esqueciSenha);
        routes.post("/api/redefinir-senha", AuthController::redefinirSenha);
        routes.post("/api/login", AuthController::login);
    }
}
