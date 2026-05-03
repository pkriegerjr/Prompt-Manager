import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class App {
    static List<String[]> usuarios = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/api/usuarios", new CadastroHandler());
        server.createContext("/api/login", new LoginHandler());

        server.setExecutor(null);
        System.out.println("Servidor iniciado em http://localhost:8081");
        server.start();
    }

    //Cadastro de usuários em server

    static class CadastroHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;

            if ("POST".equalsIgnoreCase(method)) {
                String body = lerBody(exchange);

                try {
                    String email    = extrairValor(body, "email");
                    String senha    = extrairValor(body, "password");
                    String username = extrairValor(body, "username");

                    validarEmail(email);
                    validarSenha(senha);

                    // Verifica se email já está cadastrado
                    for (String[] u : usuarios) {
                        if (u[1].equals(email)) {
                            response   = "Erro de validação: E-mail já cadastrado.";
                            statusCode = 400;
                            enviarResposta(exchange, statusCode, response);
                            return;
                        }
                    }

                    usuarios.add(new String[]{username, email, senha});
                    response   = "Conta criada com sucesso para: " + username;
                    statusCode = 200;

                } catch (EmailException | SenhaException e) {
                    response   = "Erro de validação: " + e.getMessage();
                    statusCode = 400;
                } catch (Exception e) {
                    response   = "Erro interno no servidor.";
                    statusCode = 500;
                }

            } else if ("GET".equalsIgnoreCase(method)) {
                StringBuilder sb = new StringBuilder("Lista de usuários: [");
                for (String[] u : usuarios) sb.append(u[0]).append(" (").append(u[1]).append("), ");
                response   = sb.append("]").toString();
                statusCode = 200;
            } else {
                response   = "Método não permitido.";
                statusCode = 405;
            }

            enviarResposta(exchange, statusCode, response);
        }
    }

    //Login de usuários em server

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            String response = "";
            int statusCode  = 200;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = lerBody(exchange);

                try {
                    String email = extrairValor(body, "email");
                    String senha = extrairValor(body, "password");

                    String nomeEncontrado = null;
                    for (String[] u : usuarios) {
                        if (u[1].equals(email) && u[2].equals(senha)) {
                            nomeEncontrado = u[0];
                            break;
                        }
                    }

                    if (nomeEncontrado != null) {
                        response   = "Login realizado com sucesso! Bem-vindo, " + nomeEncontrado + ".";
                        statusCode = 200;
                    } else {
                        response   = "E-mail ou senha incorretos.";
                        statusCode = 401;
                    }

                } catch (Exception e) {
                    response   = "Erro interno no servidor.";
                    statusCode = 500;
                }
            } else {
                response   = "Método não permitido.";
                statusCode = 405;
            }

            enviarResposta(exchange, statusCode, response);
        }
    }

    //utilitários

    private static String lerBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String extrairValor(String json, String campo) {
        try {
            int index = json.indexOf("\"" + campo + "\"") + campo.length() + 3;
            int fim   = json.indexOf("\"", index + 1);
            return json.substring(index + 1, fim);
        } catch (Exception e) { return ""; }
    }

    private static void enviarResposta(HttpExchange exchange, int status, String response)
            throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ─── Validações ──────────────────────────────────────────────────────────────

    public static void validarEmail(String email) throws EmailException {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new EmailException("Email inválido: " + email);
        }
    }

    public static void validarSenha(String senha) throws SenhaException {
        if (senha == null || senha.length() < 9) {
            throw new SenhaException("Senha deve conter pelo menos 9 caracteres.");
        }
    }
}