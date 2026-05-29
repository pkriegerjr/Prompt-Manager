package routes;

import controller.AdminController;
import io.javalin.router.JavalinDefaultRoutingApi;

public final class AdminRoutes {
    private AdminRoutes() {}

    public static void register(JavalinDefaultRoutingApi routes) {
        routes.get("/api/admin/stats", AdminController::stats);
        routes.get("/api/admin/usuarios", AdminController::usuarios);
        routes.post("/api/admin/usuarios/{id}/ativar", AdminController::ativarUsuario);
        routes.post("/api/admin/usuarios/{id}/desativar", AdminController::desativarUsuario);
        routes.post("/api/admin/tornar-admin", AdminController::tornarAdmin);
        routes.post("/api/admin/revogar-admin", AdminController::revogarAdmin);
        routes.delete("/api/admin/deletar-usuario/{id}", AdminController::deletarUsuario);
        routes.get("/api/admin/prompts", AdminController::prompts);
        routes.put("/api/admin/prompts/{id}", AdminController::atualizarPrompt);
        routes.delete("/api/admin/prompts/{id}", AdminController::deletarPrompt);
        routes.get("/api/admin/logs", AdminController::logs);
        routes.post("/api/admin/criar-admin", AdminController::criarAdmin);
    }
}
