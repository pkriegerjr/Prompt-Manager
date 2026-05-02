
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

public class App {
    // Simulação de banco de dados em memória
     static List<String> usuarios = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // Cria um servidor HTTP na porta 8081
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        
        // Cria o endpoint /api/usuarios
        server.createContext("/api/usuarios", new CadastroHandler());
        
        server.setExecutor(null); 
        System.out.println("Servidor iniciado em http://localhost:8081");
        server.start();
    }

    static class CadastroHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Configuração de CORS para permitir que o HTML local acesse o Java
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
                InputStream is = exchange.getRequestBody();
                
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                String body = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                
                try {
                    String email = extrairValor(body, "email");
                    String senha = extrairValor(body, "password");
                    String username = extrairValor(body, "username");

                    validarEmail(email);
                    validarSenha(senha);

                    usuarios.add(username + " (" + email + ")");
                    response = "Conta criada com sucesso para: " + username;
                    statusCode = 200;
                } catch (EmailException | SenhaException e) {
                    response = "Erro de validação: " + e.getMessage();
                    statusCode = 400;
                } catch (Exception e) {
                    response = "Erro interno no servidor.";
                    statusCode = 500;
                }
            } else if ("GET".equalsIgnoreCase(method)) {
                response = "Lista de usuários: " + usuarios.toString();
                statusCode = 200;
            } else {
                statusCode = 405;
                response = "Método não permitido";
            }

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        private String extrairValor(String json, String campo) {
            try {
                int index = json.indexOf("\"" + campo + "\"") + campo.length() + 3;
                int fim = json.indexOf("\"", index + 1);
                return json.substring(index + 1, fim);
            } catch (Exception e) { return ""; }
        }
    }

    // Método para validar o email do usuário
    public static void validarEmail(String email) throws EmailException {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new EmailException("Email invalido: " + email);
        }
    }

    //método para validar a senha do usuário
    public static void validarSenha(String senha) throws SenhaException {
        if (senha.length() < 9) {
            throw new SenhaException("Senha deve conter pelo menos 9 caracteres.");
        }
    }
}