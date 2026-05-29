package util;

import config.AppConfig;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class SessionToken {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private SessionToken() {}

    public static String emitir(String tipo, int id) {
        long expiraEm = Instant.now()
            .plusSeconds(AppConfig.SESSION_TTL_HOURS * 3600L)
            .getEpochSecond();
        String payload = encode(tipo + ":" + id + ":" + expiraEm);
        return payload + "." + assinar(payload);
    }

    public static Dados validar(String token) {
        String value = token == null ? "" : token.trim();
        int dot = value.indexOf('.');
        if (dot <= 0 || dot == value.length() - 1) {
            throw new IllegalArgumentException("Token de sessao invalido.");
        }

        String payload = value.substring(0, dot);
        String assinatura = value.substring(dot + 1);
        if (!assinaturaValida(payload, assinatura)) {
            throw new IllegalArgumentException("Token de sessao invalido.");
        }

        String[] partes = decode(payload).split(":");
        if (partes.length != 3) {
            throw new IllegalArgumentException("Token de sessao invalido.");
        }

        int id = parseId(partes[1]);
        long expiraEm = parseExpiracao(partes[2]);
        if (Instant.now().getEpochSecond() > expiraEm) {
            throw new IllegalArgumentException("Sessao expirada.");
        }

        return new Dados(partes[0], id);
    }

    private static boolean assinaturaValida(String payload, String assinatura) {
        return MessageDigest.isEqual(
            assinar(payload).getBytes(StandardCharsets.UTF_8),
            assinatura.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String assinar(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(AppConfig.SESSION_SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao assinar token de sessao.", e);
        }
    }

    private static String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        try {
            return new String(DECODER.decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Token de sessao invalido.");
        }
    }

    private static int parseId(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0) return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Token de sessao invalido.");
        }
        throw new IllegalArgumentException("Token de sessao invalido.");
    }

    private static long parseExpiracao(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Token de sessao invalido.");
        }
    }

    public record Dados(String tipo, int id) {}
}
