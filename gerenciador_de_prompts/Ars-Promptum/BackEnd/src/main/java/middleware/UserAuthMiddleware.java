package middleware;

import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import util.SessionToken;

public final class UserAuthMiddleware {
    private static final String USER_ID_ATTR = "usuarioId";

    private UserAuthMiddleware() {}

    public static void requireUser(Context ctx) {
        String token = bearerToken(ctx);
        if (token.isEmpty()) {
            throw new UnauthorizedResponse("Sessao de usuario ausente.");
        }

        SessionToken.Dados dados = validarToken(token);
        if (!"usuario".equals(dados.tipo())) {
            throw new UnauthorizedResponse("Acesso restrito a usuarios.");
        }

        ctx.attribute(USER_ID_ATTR, dados.id());
    }

    public static int currentUserId(Context ctx) {
        Integer userId = ctx.attribute(USER_ID_ATTR);
        return userId == null ? 0 : userId;
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
