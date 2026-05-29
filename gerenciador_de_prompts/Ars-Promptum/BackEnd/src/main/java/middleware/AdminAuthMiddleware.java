package middleware;

import dao.AdminDao;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import model.Admin;
import util.SessionToken;

public final class AdminAuthMiddleware {
    private static final String ADMIN_ID_ATTR = "adminId";

    private AdminAuthMiddleware() {}

    public static void requireAdmin(Context ctx) throws Exception {
        String token = bearerToken(ctx);
        if (token.isEmpty()) {
            throw new UnauthorizedResponse("Sessao de administrador ausente.");
        }

        SessionToken.Dados dados = validarToken(token);
        if (!"admin".equals(dados.tipo())) {
            throw new UnauthorizedResponse("Acesso restrito a administradores.");
        }

        Admin admin = AdminDao.buscarPorId(dados.id());
        if (admin == null) {
            throw new UnauthorizedResponse("Administrador invalido.");
        }

        ctx.attribute(ADMIN_ID_ATTR, admin.getId());
    }

    public static int currentAdminId(Context ctx) {
        Integer adminId = ctx.attribute(ADMIN_ID_ATTR);
        return adminId == null ? 1 : adminId;
    }

    private static SessionToken.Dados validarToken(String token) {
        try {
            return SessionToken.validar(token);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedResponse(e.getMessage());
        }
    }

    private static String bearerToken(Context ctx) {
        String authorization = value(ctx.header("Authorization"));
        if (!authorization.startsWith("Bearer ")) return "";
        return authorization.substring("Bearer ".length()).trim();
    }

    private static String value(String value) {
        return value == null ? "" : value.trim();
    }
}
