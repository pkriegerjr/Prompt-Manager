package dto.admin;

import model.AdminStats;

public class AdminStatsResponse {
    public final int usuarios;
    public final int usuariosAtivos;
    public final int prompts;
    public final int categorias;
    public final int logs;
    public final int[] chart;

    public AdminStatsResponse(AdminStats stats) {
        this.usuarios = stats.getUsuarios();
        this.usuariosAtivos = stats.getUsuariosAtivos();
        this.prompts = stats.getPrompts();
        this.categorias = stats.getCategorias();
        this.logs = stats.getLogs();
        this.chart = stats.getChart();
    }
}
