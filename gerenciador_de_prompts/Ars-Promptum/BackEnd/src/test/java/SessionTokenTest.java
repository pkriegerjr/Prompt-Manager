import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import util.SessionToken;

class SessionTokenTest {
    @Test
    void emittedTokenCanBeValidated() {
        String token = SessionToken.emitir("admin", 42);

        SessionToken.Dados dados = SessionToken.validar(token);

        assertEquals("admin", dados.tipo());
        assertEquals(42, dados.id());
    }

    @Test
    void rejectsMalformedToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SessionToken.validar("token-sem-assinatura")
        );

        assertTrue(exception.getMessage().contains("Token de sessao invalido"));
    }

    @Test
    void rejectsTamperedSignature() {
        String token = SessionToken.emitir("admin", 1);
        String tampered = token.substring(0, token.length() - 1) + differentLastChar(token);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SessionToken.validar(tampered)
        );

        assertTrue(exception.getMessage().contains("Token de sessao invalido"));
    }

    private static char differentLastChar(String value) {
        return value.charAt(value.length() - 1) == 'a' ? 'b' : 'a';
    }
}
