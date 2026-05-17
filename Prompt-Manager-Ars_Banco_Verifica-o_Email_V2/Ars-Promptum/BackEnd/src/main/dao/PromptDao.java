package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Prompt;

public final class PromptDao {
    private PromptDao() {}

    public static Prompt buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT p.*, c.nome AS categoria, NULL AS username FROM prompts p LEFT JOIN categorias c ON p.categoria_id=c.id WHERE p.id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public static List<Prompt> listarPorUsuario(int usuarioId) throws SQLException {
        List<Prompt> prompts = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT p.*, c.nome AS categoria, NULL AS username FROM prompts p LEFT JOIN categorias c ON p.categoria_id=c.id WHERE p.usuario_id=? ORDER BY p.criado_em DESC")) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) prompts.add(map(rs));
        }
        return prompts;
    }

    public static List<Prompt> listarTodosAdmin() throws SQLException {
        List<Prompt> prompts = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT p.*,u.username,c.nome AS categoria FROM prompts p JOIN usuarios u ON p.usuario_id=u.id LEFT JOIN categorias c ON p.categoria_id=c.id ORDER BY p.criado_em DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) prompts.add(map(rs));
        }
        return prompts;
    }

    public static void criar(int usuarioId, int categoriaId, String titulo, String conteudo, String descricao) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO prompts (usuario_id,categoria_id,titulo,conteudo,descricao) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, usuarioId);
            if (categoriaId > 0) ps.setInt(2, categoriaId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, titulo);
            ps.setString(4, conteudo);
            ps.setString(5, descricao);
            ps.executeUpdate();
        }
    }

    public static boolean atualizar(int id, int categoriaId, String titulo, String conteudo, String descricao) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE prompts SET titulo=?,conteudo=?,categoria_id=?,descricao=? WHERE id=?")) {
            ps.setString(1, titulo);
            ps.setString(2, conteudo);
            if (categoriaId > 0) ps.setInt(3, categoriaId); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, descricao);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    public static void atualizarAdmin(int id, int categoriaId, String titulo, String conteudo) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE prompts SET titulo=?,conteudo=?,categoria_id=? WHERE id=?")) {
            ps.setString(1, titulo);
            ps.setString(2, conteudo);
            if (categoriaId > 0) ps.setInt(3, categoriaId); else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public static boolean deletar(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM prompts WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Prompt map(ResultSet rs) throws SQLException {
        return new Prompt(
            rs.getInt("id"),
            rs.getInt("usuario_id"),
            rs.getInt("categoria_id"),
            rs.getString("categoria"),
            rs.getString("username"),
            rs.getString("titulo"),
            rs.getString("conteudo"),
            rs.getString("descricao"),
            String.valueOf(rs.getTimestamp("criado_em")),
            String.valueOf(rs.getTimestamp("atualizado_em"))
        );
    }
}
