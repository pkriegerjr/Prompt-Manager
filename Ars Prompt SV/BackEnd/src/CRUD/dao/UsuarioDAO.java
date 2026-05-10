import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para a tabela `usuarios`.
 * Toda operação de banco relacionada a usuários passa por aqui.
 */
public class UsuarioDAO {

    // ── CRIAR ──────────────────────────────────────────────
    /**
     * Insere um novo usuário no banco.
     * A senha já deve chegar como hash BCrypt.
     */
    public boolean criar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (username, email, password) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Email ou username duplicado
            throw new SQLException("Email ou nome de usuário já cadastrado.", e);
        }
    }

    // ── LER POR EMAIL (login) ───────────────────────────────
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            return null;   // não encontrado
        }
    }

    // ── LER TODOS (admin) ───────────────────────────────────
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY criado_em DESC";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── ATIVAR / DESATIVAR (admin) ──────────────────────────
    public boolean setAtivo(int id, int ativo) throws SQLException {
        String sql = "UPDATE usuarios SET ativo = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ativo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── CONTAR (dashboard admin) ────────────────────────────
    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int contarAtivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE ativo = 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── HELPER: mapeia ResultSet → Usuario ──────────────────
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("ativo"),
            rs.getString("role"),
            rs.getString("criado_em")
        );
    }
}
