package util;

public final class JsonUtil {
    private JsonUtil() {}

    public static String str(String body, String campo) {
        try {
            int idx = body.indexOf("\"" + campo + "\"");
            if (idx < 0) return "";
            int colon = body.indexOf(":", idx);
            int q1 = body.indexOf("\"", colon + 1);
            if (q1 < 0) return "";
            int q2 = body.indexOf("\"", q1 + 1);
            return body.substring(q1 + 1, q2);
        } catch (Exception e) {
            return "";
        }
    }

    public static String num(String body, String campo) {
        try {
            int idx = body.indexOf("\"" + campo + "\"");
            if (idx < 0) return "";
            int colon = body.indexOf(":", idx);
            StringBuilder num = new StringBuilder();
            for (int i = colon + 1; i < body.length(); i++) {
                char c = body.charAt(i);
                if (Character.isDigit(c)) num.append(c);
                else if (c == ' ' || c == '\n') {
                    if (num.length() == 0) continue;
                    break;
                } else break;
            }
            return num.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String or(String body, String campo, String def) {
        String value = str(body, campo);
        return (value == null || value.isEmpty()) ? def : value;
    }

    public static int parseIntOrDefault(String value, int def) {
        try { return Integer.parseInt(value); }
        catch (Exception e) { return def; }
    }

    public static int queryInt(String query, String param) {
        if (query == null) return 0;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                try { return Integer.parseInt(kv[1]); }
                catch (Exception e) { return 0; }
            }
        }
        return 0;
    }

    public static String queryString(String query, String param) {
        if (query == null) return "";
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) return kv[1];
        }
        return "";
    }

    public static String esc(String value) {
        return value == null ? "" : value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "");
    }

    public static String rsString(java.sql.ResultSet rs, String col) {
        try { return rs.getString(col); }
        catch (java.sql.SQLException e) { return ""; }
    }
}
