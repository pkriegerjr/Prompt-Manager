package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Categoria;

public final class CategoriaDao {
    private CategoriaDao() {}

    public static List<Categoria> listarTodas() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM categorias ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new Categoria(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("descricao")
                ));
            }
        }
        return categorias;
    }

    public static boolean existe(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT 1 FROM categorias WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public static int buscarOuCriar(String nome) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT id FROM categorias WHERE nome=?")) {
            ps.setString(1, nome);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }

        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO categorias (nome,descricao) VALUES (?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, "Criada automaticamente pelo usuario");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLIntegrityConstraintViolationException ignored) {
            try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT id FROM categorias WHERE nome=?")) {
                ps.setString(1, nome);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt("id");
            }
        }
        return 0;
    }

    public static void criar(String nome, String descricao) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("INSERT INTO categorias (nome,descricao) VALUES (?,?)")) {
            ps.setString(1, nome);
            ps.setString(2, descricao);
            ps.executeUpdate();
        }
    }

    public static void atualizar(int id, String nome, String descricao) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("UPDATE categorias SET nome=?,descricao=? WHERE id=?")) {
            ps.setString(1, nome);
            ps.setString(2, descricao);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public static void deletar(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM categorias WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
