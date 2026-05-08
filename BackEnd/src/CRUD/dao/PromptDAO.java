import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a tabela `prompts`.
 */
public class PromptDAO {

    // ── CRIAR ───────────────────────────────────────────────
    public boolean criar(Prompt p) throws SQLException {
        String sql = "INSERT INTO prompts (usuario_id, categoria_id, titulo, conteudo) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getUsuarioId());
            if (p.getCategoriaId() > 0)
                ps.setInt(2, p.getCategoriaId());
            else
                ps.setNull(2, Types.INTEGER);
            ps.setString(3, p.getTitulo());
            ps.setString(4, p.getConteudo());
            ps.executeUpdate();
            return true;
        }
    }

    // ── LISTAR POR USUÁRIO ──────────────────────────────────
    public List<Prompt> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT * FROM prompts WHERE usuario_id = ? ORDER BY criado_em DESC";
        List<Prompt> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── LISTAR TODOS (admin) ────────────────────────────────
    public List<Prompt> listarTodos() throws SQLException {
        String sql = "SELECT p.*, u.username FROM prompts p " +
                     "JOIN usuarios u ON p.usuario_id = u.id " +
                     "ORDER BY p.criado_em DESC";
        List<Prompt> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── BUSCAR POR ID ───────────────────────────────────────
    public Prompt buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM prompts WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        }
    }

    // ── EDITAR ──────────────────────────────────────────────
    public boolean editar(Prompt p) throws SQLException {
        String sql = "UPDATE prompts SET titulo = ?, conteudo = ?, categoria_id = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getTitulo());
            ps.setString(2, p.getConteudo());
            if (p.getCategoriaId() > 0)
                ps.setInt(3, p.getCategoriaId());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setInt(4, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ── DELETAR ─────────────────────────────────────────────
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM prompts WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── CONTAR (dashboard) ──────────────────────────────────
    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM prompts";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── HELPER ──────────────────────────────────────────────
    private Prompt mapear(ResultSet rs) throws SQLException {
        return new Prompt(
            rs.getInt("id"),
            rs.getInt("usuario_id"),
            rs.getInt("categoria_id"),
            rs.getString("titulo"),
            rs.getString("conteudo"),
            rs.getString("criado_em"),
            rs.getString("atualizado_em")
        );
    }
}
