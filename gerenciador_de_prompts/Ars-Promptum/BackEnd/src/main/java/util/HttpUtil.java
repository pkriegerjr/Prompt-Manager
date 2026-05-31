package util;

import io.javalin.http.Context;

public final class HttpUtil {
    private HttpUtil() {}

    public static void text(Context ctx, int status, String msg) {
        ctx.status(status);
        ctx.contentType("text/plain; charset=UTF-8");
        ctx.result(msg);
    }

    public static void json(Context ctx, int status, String json) {
        ctx.status(status);
        ctx.contentType("application/json; charset=UTF-8");
        ctx.result(json);
    }

    public static void html(Context ctx, String html) {
        ctx.status(200);
        ctx.contentType("text/html; charset=UTF-8");
        ctx.result(html);
    }

    public static String body(Context ctx) {
        return ctx.body();
    }
}
