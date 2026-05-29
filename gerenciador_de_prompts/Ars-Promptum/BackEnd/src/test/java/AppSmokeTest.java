import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.SessionToken;

class AppSmokeTest {
    private Javalin app;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void startServer() {
        app = App.createApp().start(0);
        client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
        baseUrl = "http://localhost:" + app.port();
    }

    @AfterEach
    void stopServer() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void rootRedirectsToFrontendIndex() throws Exception {
        HttpResponse<String> response = get("/");

        assertEquals(302, response.statusCode());
        assertEquals("/pages/index.html", response.headers().firstValue("Location").orElse(""));
    }

    @Test
    void servesFrontendIndexFromJavalin() throws Exception {
        HttpResponse<String> response = get("/pages/index.html");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Ars Prompt"));
    }

    @Test
    void promptRoutesRequireUserSessionBeforeDatabaseAccess() throws Exception {
        HttpResponse<String> response = get("/api/prompts");

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Sessao de usuario ausente"));
    }

    @Test
    void promptsWithoutUserIdReturnsBadRequestBeforeDatabaseAccess() throws Exception {
        HttpResponse<String> response = get("/api/prompts", userToken());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Usuario invalido"));
    }

    @Test
    void invalidPathParamUsesGlobalExceptionHandler() throws Exception {
        HttpResponse<String> response = get("/api/prompts/abc", userToken());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("\"erro\""));
    }

    @Test
    void invalidRegisterPayloadReturnsBadRequestBeforeDatabaseAccess() throws Exception {
        HttpResponse<String> response = postJson(
            "/api/usuarios",
            "{\"username\":\"teste\",\"email\":\"email-invalido\",\"password\":\"123\"}"
        );

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Email invalido"));
    }

    @Test
    void invalidPromptPayloadReturnsBadRequestBeforeDatabaseAccess() throws Exception {
        HttpResponse<String> response = postJson(
            "/api/prompts",
            "{\"usuarioId\":1,\"titulo\":\"\",\"conteudo\":\"\"}",
            userToken()
        );

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Titulo e conteudo sao obrigatorios"));
    }

    @Test
    void promptRoutesRejectAdminTokenBeforeDatabaseAccess() throws Exception {
        HttpResponse<String> response = get("/api/prompts?uid=1", SessionToken.emitir("admin", 1));

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Acesso restrito a usuarios"));
    }

    @Test
    void promptRoutesRejectMismatchedUserTokenBeforeDatabaseAccess() throws Exception {
        HttpResponse<String> response = get("/api/prompts?uid=2", userToken());

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Sessao de usuario invalida"));
    }

    @Test
    void adminRoutesRequireAdminSessionBeforeDatabaseAccess() throws Exception {
        HttpResponse<String> response = get("/api/admin/stats");

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Sessao de administrador ausente"));
    }

    @Test
    void adminRoutesRejectInvalidBearerTokenBeforeDatabaseAccess() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/admin/stats"))
            .header("Authorization", "Bearer token-invalido")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Token de sessao invalido"));
    }

    @Test
    void adminRoutesRejectValidNonAdminTokenBeforeDatabaseAccess() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/admin/stats"))
            .header("Authorization", "Bearer " + SessionToken.emitir("usuario", 1))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Acesso restrito a administradores"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .GET()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String body, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String userToken() {
        return SessionToken.emitir("usuario", 1);
    }
}
