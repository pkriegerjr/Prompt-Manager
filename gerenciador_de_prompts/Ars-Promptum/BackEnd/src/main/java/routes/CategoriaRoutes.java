package routes;

import controller.CategoriaController;
import io.javalin.router.JavalinDefaultRoutingApi;

public final class CategoriaRoutes {
    private CategoriaRoutes() {}

    public static void register(JavalinDefaultRoutingApi routes) {
        routes.get("/api/categorias", CategoriaController::categorias);
        routes.get("/api/admin/categorias", CategoriaController::adminCategorias);
        routes.post("/api/admin/categorias", CategoriaController::criarAdminCategoria);
        routes.put("/api/admin/categorias/{id}", CategoriaController::atualizarAdminCategoria);
        routes.delete("/api/admin/categorias/{id}", CategoriaController::deletarAdminCategoria);
    }
}
