
 
public class SenhaException extends Exception {
    /**
     * Construtor que aceita uma mensagem de erro.
     * @param message Mensagem detalhando o erro de validação.
     */
    public SenhaException(String message) {
        super(message);
    }
}