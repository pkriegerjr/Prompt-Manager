package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class AppConfig {
    private static final Properties CONFIG = carregarConfig();

    public static final String DB_URL = cfg("DB_URL", "jdbc:mysql://localhost:3306/ars_database?useSSL=false&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8");
    public static final String DB_USER = cfg("DB_USER", "root");
    public static final String DB_PASS = cfg("DB_PASS", "");

    public static final String SMTP_HOST = cfg("SMTP_HOST", "smtp.gmail.com");
    public static final int SMTP_PORT = cfgInt("SMTP_PORT", 587);
    public static final String SMTP_USER = cfg("SMTP_USER", "");
    public static final String SMTP_PASS = cfg("SMTP_PASS", "");
    public static final String BASE_URL = cfg("BASE_URL", "http://localhost:8081/pages");
    public static final String SESSION_SECRET = cfg("SESSION_SECRET", "ars-promptum-dev-session-secret");
    public static final int SESSION_TTL_HOURS = cfgInt("SESSION_TTL_HOURS", 8);

    private AppConfig() {}

    private static Properties carregarConfig() {
        Properties props = new Properties();
        String[] caminhos = {"config.env", "../config.env", "../../config.env"};
        for (String caminho : caminhos) {
            File arquivo = new File(caminho);
            if (!arquivo.isFile()) continue;
            try (FileInputStream in = new FileInputStream(arquivo)) {
                props.load(in);
                System.out.println("[CONFIG] Usando configuracao local: " + arquivo.getPath());
            } catch (IOException e) {
                System.err.println("[CONFIG] Nao foi possivel ler " + arquivo.getPath() + ": " + e.getMessage());
            }
            break;
        }
        return props;
    }

    private static String cfg(String chave, String padrao) {
        String ambiente = System.getenv(chave);
        if (ambiente != null && !ambiente.isBlank()) return ambiente.trim();
        String arquivo = CONFIG.getProperty(chave);
        return (arquivo == null || arquivo.isBlank()) ? padrao : arquivo.trim();
    }

    private static int cfgInt(String chave, int padrao) {
        try { return Integer.parseInt(cfg(chave, String.valueOf(padrao))); }
        catch (NumberFormatException e) { return padrao; }
    }
}
