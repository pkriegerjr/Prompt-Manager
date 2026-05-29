package model;

public class AdminStats {
    private final int usuarios;
    private final int usuariosAtivos;
    private final int prompts;
    private final int categorias;
    private final int logs;
    private final int[] chart;

    public AdminStats(int usuarios, int usuariosAtivos, int prompts, int categorias, int logs, int[] chart) {
        this.usuarios = usuarios;
        this.usuariosAtivos = usuariosAtivos;
        this.prompts = prompts;
        this.categorias = categorias;
        this.logs = logs;
        this.chart = chart;
    }

    public int getUsuarios() { return usuarios; }
    public int getUsuariosAtivos() { return usuariosAtivos; }
    public int getPrompts() { return prompts; }
    public int getCategorias() { return categorias; }
    public int getLogs() { return logs; }
    public int[] getChart() { return chart; }
}
