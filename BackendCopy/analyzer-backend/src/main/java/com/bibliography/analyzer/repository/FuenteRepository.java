package com.bibliography.analyzer.repository;

import com.bibliography.analyzer.dominio.url.WebMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "app.modulo.url.enabled", havingValue = "true", matchIfMissing = true)
public class FuenteRepository {

    public static class DataBaseException extends RuntimeException {
        public DataBaseException(String mensaje, SQLException causa) { super(mensaje, causa); }
    }

    public record FuenteRow(long id, String url, String domain, String title, String topic,
                            String author, String publishedDate, String textContent, String createdAt) {}

    private final String dbUrl;

    public FuenteRepository() {
        String env = System.getenv("DB_URL");
        String prop = System.getProperty("db.url");
        this.dbUrl = (env != null && !env.isBlank()) ? env.trim()
                : (prop != null && !prop.isBlank()) ? prop.trim()
                : "jdbc:sqlite:metadata.db";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC de SQLite no encontrado (revisa la dependencia sqlite-jdbc del pom).", e);
        }
        initSchema();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS web_metadata ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url TEXT NOT NULL UNIQUE, "
                + "domain TEXT NOT NULL, "
                + "title TEXT, "
                + "author TEXT, "
                + "published_date TEXT, "
                + "text_content TEXT, "
                + "topic TEXT, "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP);";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DataBaseException("No se pudo inicializar la base de datos SQLite.", e);
        }
    }

    public boolean exists(String url) {
        String sql = "SELECT COUNT(*) FROM web_metadata WHERE url = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new DataBaseException("Error al verificar existencia.", e);
        }
    }

    public void save(WebMetadata m) {
        String sql = "INSERT INTO web_metadata (url, domain, title, author, published_date, text_content, topic) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(url) DO UPDATE SET title = excluded.title, author = excluded.author, "
                + "published_date = excluded.published_date, text_content = excluded.text_content, "
                + "topic = excluded.topic, created_at = CURRENT_TIMESTAMP";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.url());
            ps.setString(2, m.domain());
            ps.setString(3, m.title());
            ps.setString(4, m.author());
            ps.setString(5, m.publishedDate());
            ps.setString(6, m.textContent());
            ps.setString(7, m.topic());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataBaseException("Error al guardar metadatos.", e);
        }
    }

    public Optional<FuenteRow> findByUrl(String url) {
        String sql = "SELECT id, url, domain, title, author, published_date, text_content, topic, created_at "
                + "FROM web_metadata WHERE url = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataBaseException("Error al consultar por URL.", e);
        }
    }

    public Optional<FuenteRow> findById(long id) {
        String sql = "SELECT id, url, domain, title, author, published_date, text_content, topic, created_at "
                + "FROM web_metadata WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataBaseException("Error al consultar por id.", e);
        }
    }

    public List<FuenteRow> findAll() {
        List<FuenteRow> out = new ArrayList<>();
        String sql = "SELECT id, url, domain, title, author, published_date, text_content, topic, created_at "
                + "FROM web_metadata ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new DataBaseException("Error al listar fuentes.", e);
        }
    }

    public boolean update(long id, String title, String author, String topic) {
        List<String> sets = new ArrayList<>();
        List<String> values = new ArrayList<>();
        if (title != null)  { sets.add("title = ?");  values.add(title); }
        if (author != null) { sets.add("author = ?"); values.add(author); }
        if (topic != null)  { sets.add("topic = ?");  values.add(topic); }
        if (sets.isEmpty()) return findById(id).isPresent();
        sets.add("created_at = CURRENT_TIMESTAMP");
        String sql = "UPDATE web_metadata SET " + String.join(", ", sets) + " WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (String v : values) ps.setString(i++, v);
            ps.setLong(i, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataBaseException("Error al actualizar fuente.", e);
        }
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM web_metadata WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataBaseException("Error al eliminar fuente.", e);
        }
    }

    public boolean hasMatchingTopicCount(String topic, String excludeUrl, int expectedCount) {
        if (topic == null || topic.isBlank()) return expectedCount == 0;
        List<String> keywords = keywords(topic);
        if (keywords.isEmpty()) return expectedCount == 0;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM web_metadata WHERE (");
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("LOWER(topic) LIKE LOWER(?)");
        }
        sql.append(")");
        boolean exclude = excludeUrl != null && !excludeUrl.isBlank();
        if (exclude) sql.append(" AND url != ?");
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (String k : keywords) ps.setString(i++, "%" + k + "%");
            if (exclude) ps.setString(i++, excludeUrl.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) >= expectedCount;
        } catch (SQLException e) {
            throw new DataBaseException("Error al contar instancias por tópico.", e);
        }
    }

    public List<FuenteRow> similaresPorTopic(String topic, String excludeUrl, int limit) {
        List<FuenteRow> out = new ArrayList<>();
        if (topic == null || topic.isBlank() || limit <= 0) return out;
        List<String> keywords = keywords(topic);
        if (keywords.isEmpty()) return out;
        StringBuilder sql = new StringBuilder(
            "SELECT id, url, domain, title, author, published_date, text_content, topic, created_at FROM web_metadata WHERE (");
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("LOWER(topic) LIKE LOWER(?)");
        }
        sql.append(")");
        boolean exclude = excludeUrl != null && !excludeUrl.isBlank();
        if (exclude) sql.append(" AND url != ?");
        sql.append(" LIMIT ?");
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (String k : keywords) ps.setString(i++, "%" + k + "%");
            if (exclude) ps.setString(i++, excludeUrl.trim());
            ps.setInt(i, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new DataBaseException("Error al buscar similares por tópico.", e);
        }
    }

    private static List<String> keywords(String topic) {
        return Arrays.stream(topic.split("[/,\\s]+"))
                .map(String::trim)
                .filter(term -> term.length() > 2)
                .collect(Collectors.toList());
    }

    private static FuenteRow map(ResultSet rs) throws SQLException {
        return new FuenteRow(
                rs.getLong("id"),
                rs.getString("url"),
                rs.getString("domain"),
                rs.getString("title"),
                rs.getString("topic"),
                rs.getString("author"),
                rs.getString("published_date"),
                rs.getString("text_content"),
                rs.getString("created_at"));
    }
}