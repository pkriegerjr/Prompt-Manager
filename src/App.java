import java.util.Scanner;
public class App {
    public static void main(String[] args) throws Exception {
        //Scanner para ler a entrada do usuário
        Scanner sc = new Scanner(System.in);
        System.out.println("Seja bem-vindo ao Ars Prompt!");

        // Pergunta se o usuário já tem uma conta
        System.out.println("É cadastrado? (S/N)");
        String resposta = sc.nextLine();

        //Caso o usuário já tenha uma conta, ele pode fazer login
        if (resposta.equalsIgnoreCase("S")) {
            System.out.println("Digite seu nome de usuário:");
            String username = sc.nextLine();
            System.out.println("Digite sua senha:");
            String password = sc.nextLine();

            //Login efetuado com sucesso
            System.out.println("Login bem-sucedido! Bem-vindo, " + username + "!");

            //Caso o usuário não tenha uma conta.
        } else {
            System.out.println("Vamos criar uma nova conta para você.");

            System.out.println("Digite um nome de usuário:");
            String newUsername = sc.nextLine();
            
            // Validação do email
            boolean emailValido = false;
            do{
            System.out.println("Digite seu email:");
            String email = sc.nextLine();
            try {
            validarEmail(email);
            emailValido = true;
        } catch (EmailException e) {
            System.out.println("Erro: " + e.getMessage());
        }
            } while (!emailValido);

            //criando a senha do usuário
            boolean senhaValida = false;
            do{
            System.out.println("Digite uma senha:");
            String newPassword = sc.nextLine();
            try {
                validarSenha(newPassword);
                senhaValida = true;
            } catch (SenhaException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }while (!senhaValida);

            // conta criada com sucesso
            System.out.println("Conta criada com sucesso! Bem-vindo, " + newUsername + "!");
        }
        sc.close();
    }


    // Método para validar o email do usuário
    public static void validarEmail(String email) throws EmailException {
        if (!email.contains("@") || !email.contains(".")) {
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