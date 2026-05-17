package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class SecurityUtil {
    private SecurityUtil() {}

    public static void validarEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email invalido: " + email);
        }
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < 9) {
            throw new IllegalArgumentException("Senha deve ter no minimo 9 caracteres.");
        }
    }

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro SHA-256", e);
        }
    }
}
