import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a tabela `categorias`.
 */
public class CategoriaDAO {

    public boolean criar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categorias (nome, descricao) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getDescricao());
            ps.executeUpdate();
            return true;
        }
    }

    public List<Categoria> listarTodas() throws SQLException {
        String sql = "SELECT * FROM categorias ORDER BY nome";
        List<Categoria> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public boolean editar(Categoria c) throws SQLException {
        String sql = "UPDATE categorias SET nome = ?, descricao = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getDescricao());
            ps.setInt(3, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM categorias";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("descricao")
        );
    }
}
