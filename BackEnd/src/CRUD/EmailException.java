
/**
 * Exceção personalizada para erros relacionados à validação de e-mail.
 */
public class EmailException extends Exception {
    /**
     * Construtor que aceita uma mensagem de erro.
     * @param message Mensagem detalhando o erro de validação.
     */
    public EmailException(String message) {
        super(message);
    }
}