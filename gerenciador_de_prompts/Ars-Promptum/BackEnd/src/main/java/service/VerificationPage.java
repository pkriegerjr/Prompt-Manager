package service;

import config.AppConfig;

public final class VerificationPage {
    private VerificationPage() {}

    public static String resultado(String titulo, String msg, boolean sucesso) {
        String cor = sucesso ? "#3fb950" : "#f85149";
        String btnUrl = AppConfig.BASE_URL + "/View.html";
        return "<!DOCTYPE html><html lang='pt-br'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Ars Prompt - Verificacao</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;600;700&display=swap' rel='stylesheet'>" +
            "<style>*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:'Space Grotesk',sans-serif;background:#0d1117;color:#e6edf3;" +
            "min-height:100vh;display:flex;align-items:center;justify-content:center;padding:1.5rem}" +
            ".card{background:#161b22;border:1px solid #30363d;border-radius:12px;padding:2.5rem 2rem;" +
            "max-width:420px;width:100%;text-align:center;box-shadow:0 8px 40px rgba(0,0,0,.5)}" +
            ".icon{font-size:3rem;margin-bottom:1rem}" +
            "h1{font-size:1.5rem;font-weight:700;margin-bottom:.75rem;color:" + cor + "}" +
            "p{color:#8b949e;font-size:.9rem;line-height:1.6;margin-bottom:1.75rem}" +
            ".brand{font-family:monospace;font-size:.85rem;color:#58a6ff;margin-bottom:1.5rem}" +
            "a{display:inline-block;background:#58a6ff;color:#0d1117;font-weight:700;" +
            "padding:.75rem 2rem;border-radius:8px;text-decoration:none;font-size:.95rem;transition:background .2s}" +
            "a:hover{background:#79b8ff}</style></head><body>" +
            "<div class='card'><div class='brand'>{ } ars-prompt</div>" +
            "<div class='icon'>" + (sucesso ? "OK" : "!") + "</div>" +
            "<h1>" + titulo + "</h1><p>" + msg + "</p>" +
            (sucesso ? "<a href='" + btnUrl + "'>Ir para o login</a>" : "") +
            "</div></body></html>";
    }
}
