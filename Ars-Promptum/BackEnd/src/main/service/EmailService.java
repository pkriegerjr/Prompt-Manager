package service;

import config.AppConfig;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class EmailService {
    private EmailService() {}

    public static void enviarLinkVerificacao(String destino, String nome, String token, boolean isAdmin) {
        try {
            String link = "http://localhost:8081/api/verificar?token=" + token
                + (isAdmin ? "&tipo=admin" : "");
            String papel = isAdmin ? "administrador" : "usuario";

            String html =
                "<div style=\"font-family:monospace;background:#0d1117;color:#e6edf3;padding:40px;border-radius:12px;max-width:500px;margin:0 auto\">" +
                "<p style=\"color:#58a6ff;font-size:1.1rem;margin:0 0 4px\"><b>{ } ars-prompt</b></p>" +
                "<p style=\"color:#8b949e;margin:0 0 24px;font-size:0.85rem\">Verificacao de conta</p>" +
                "<h2 style=\"margin:0 0 16px;font-size:1.4rem\">Ola, " + escaparHtml(nome) + "!</h2>" +
                "<p style=\"color:#8b949e;line-height:1.7\">Sua conta de <strong style=\"color:#e6edf3\">" + papel + "</strong> foi criada no Ars Prompt.<br>" +
                "Clique no botao abaixo para verificar seu email e ativar o acesso:</p>" +
                "<div style=\"text-align:center;margin:32px 0\">" +
                "<a href=\"" + link + "\" style=\"background:#58a6ff;color:#0d1117;font-weight:700;font-size:1rem;padding:14px 32px;border-radius:8px;text-decoration:none;display:inline-block\">Verificar minha conta</a>" +
                "</div>" +
                "<p style=\"color:#484f58;font-size:0.8rem;border-top:1px solid #30363d;padding-top:16px;margin:0\">" +
                "Link valido por 24 horas. Se nao criou esta conta, ignore este email.</p>" +
                "</div>";

            enviar(destino, "ars-prompt - Verifique sua conta", html);
            System.out.println("[EMAIL] Link enviado para: " + destino);
        } catch (Exception e) {
            System.err.println("[EMAIL] Erro ao enviar para " + destino + ": " + e.getMessage());
        }
    }

    public static void enviarEmailReset(String destino, String nome, String token, boolean isAdmin) {
        try {
            String papel = isAdmin ? "administrador" : "usuario";
            String html =
                "<div style=\"font-family:monospace;background:#0d1117;color:#e6edf3;padding:40px;border-radius:12px;max-width:500px;margin:0 auto\">" +
                "<p style=\"color:#58a6ff;font-size:1.1rem;margin:0 0 4px\"><b>{ } ars-prompt</b></p>" +
                "<p style=\"color:#8b949e;margin:0 0 24px;font-size:0.85rem\">Redefinicao de senha</p>" +
                "<h2 style=\"margin:0 0 16px;font-size:1.4rem\">Ola, " + escaparHtml(nome) + "!</h2>" +
                "<p style=\"color:#8b949e;line-height:1.7\">Recebemos um pedido para redefinir a senha da sua conta de <strong style=\"color:#e6edf3\">" + papel + "</strong>.<br>" +
                "Copie o token abaixo e cole na pagina de redefinicao:</p>" +
                "<div style=\"background:#161b22;border:1px solid #30363d;border-radius:8px;padding:20px;text-align:center;margin:24px 0\">" +
                "<code style=\"font-size:0.9rem;color:#58a6ff;word-break:break-all\">" + token + "</code>" +
                "</div>" +
                "<p style=\"color:#484f58;font-size:0.8rem;border-top:1px solid #30363d;padding-top:16px;margin:0\">" +
                "Token valido por 1 hora. Se nao solicitou isso, ignore este email.</p>" +
                "</div>";

            enviar(destino, "ars-prompt - Redefinicao de senha", html);
            System.out.println("[EMAIL] Token de reset enviado para: " + destino);
        } catch (Exception e) {
            System.err.println("[EMAIL] Erro ao enviar reset para " + destino + ": " + e.getMessage());
        }
    }

    private static void enviar(String destino, String assunto, String html) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", AppConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(AppConfig.SMTP_PORT));
        props.put("mail.smtp.ssl.trust", AppConfig.SMTP_HOST);

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(AppConfig.SMTP_USER, AppConfig.SMTP_PASS);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(AppConfig.SMTP_USER, "ars-prompt"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino));
        msg.setSubject(assunto, "UTF-8");
        msg.setContent(html, "text/html; charset=UTF-8");
        Transport.send(msg);
    }

    public static String escaparHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
