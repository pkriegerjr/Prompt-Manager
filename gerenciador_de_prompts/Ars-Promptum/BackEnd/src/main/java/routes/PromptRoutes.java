package routes;

import controller.PromptController;
import io.javalin.router.JavalinDefaultRoutingApi;

public final class PromptRoutes {
    private PromptRoutes() {}

    public static void register(JavalinDefaultRoutingApi routes) {
        routes.get("/api/prompts", PromptController::listarPorUsuario);
        routes.get("/api/prompts/{id}", PromptController::buscarPorId);
        routes.post("/api/prompts", PromptController::criar);
        routes.put("/api/prompts/{id}", PromptController::atualizar);
        routes.delete("/api/prompts/{id}", PromptController::deletar);
    }
}
