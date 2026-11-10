package com.bibliography.analyzer.service;

import com.bibliography.analyzer.dominio.url.JsonMini;
import com.bibliography.analyzer.dominio.url.TopicExtractor;
import com.bibliography.analyzer.dominio.url.WebMetadata;
import org.slf4j.Logger;                                                // <-- NUEVO
import org.slf4j.LoggerFactory;                                       // <-- NUEVO
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
@ConditionalOnProperty(name = "app.modulo.url.enabled", havingValue = "true", matchIfMissing = true)
public class ExaClient {

    private static final Logger log = LoggerFactory.getLogger(ExaClient.class); // <-- NUEVO

    public static class ExaException extends RuntimeException {
        private final int statusCode;
        public ExaException(int statusCode, String cuerpo) {
            super("Exa API respondió HTTP " + statusCode + ": " + cuerpo);
            this.statusCode = statusCode;
        }
        public int statusCode() { return statusCode; }
    }

    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(10);
    private static final Duration TIMEOUT_PETICION = Duration.ofSeconds(30);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT_CONEXION)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // Precedencia de credencial: entorno > propiedad JVM > config.properties
    private static String loadApiKey() {
        String fromEnv = System.getenv("EXA_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv.trim();
        String fromProp = System.getProperty("exa.api.key");
        if (fromProp != null && !fromProp.isBlank()) return fromProp.trim();
        Path config = Path.of("config.properties");
        if (Files.exists(config)) {
            Properties prop = new Properties();
            try (InputStream in = Files.newInputStream(config)) {
                prop.load(in);
                String k = prop.getProperty("exa.api.key");
                if (k != null && !k.isBlank()) return k.trim();
            } catch (IOException e) {
                System.err.println("Advertencia: no se pudo leer config.properties: " + e.getMessage());
            }
        }
        return null;
    }

    private final String apiKey = loadApiKey();

    private String requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Falta configurar la clave de Exa: variable EXA_API_KEY, -Dexa.api.key o config.properties.");
        }
        return apiKey;
    }

    public WebMetadata extractData(String targetUrl) throws IOException {
        String payload = "{\"urls\":[\"" + JsonMini.escape(targetUrl) + "\"],\"text\":true,\"summary\":true,\"livecrawl\":\"always\"}";
        String body = send("https://api.exa.ai/contents", payload);
        String title = JsonMini.field(body, "title");
        String author = JsonMini.field(body, "author");
        String publishedDate = JsonMini.field(body, "publishedDate");
        String textContent = JsonMini.field(body, "text");
        String topic = TopicExtractor.extractMainTopic(textContent, title);
        String domain = URI.create(targetUrl).getHost();
        return new WebMetadata(
                domain,
                targetUrl,
                title.isEmpty() ? "Sin título" : title,
                (topic == null || topic.isEmpty()) ? "Sin Topico" : topic,
                author.isEmpty() ? "Sin autor" : author,
                publishedDate.isEmpty() ? "Fecha no especificada" : publishedDate,
                textContent);
    }

    public List<String> findSimilarPagesByTopic(String topic, int maxResults) throws IOException {
        if (topic == null || topic.isBlank() || topic.equals("Sin Topico")) return List.of();
        String query = "Informative article, guide, documentation, or reference material about: " + topic;
        String payload = "{\"query\":\"" + JsonMini.escape(query) + "\",\"numResults\":" + maxResults
                + ",\"useAutoprompt\":true,\"type\":\"neural\"}";
        String body = send("https://api.exa.ai/search", payload);
        return JsonMini.urlsFromResults(body);
    }

    // ===== MÉTODO REEMPLAZADO: ahora loguea tiempo y status de cada llamada =====
    private String send(String url, String payload) throws IOException {
        long t0 = System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT_PETICION)
                .header("Content-Type", "application/json")
                .header("x-api-key", requireApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long ms = System.currentTimeMillis() - t0;
            if (response.statusCode() == 200) {
                log.info("Exa {} respondió 200 en {} ms", url, ms);
                return response.body();
            }
            log.warn("Exa {} respondió HTTP {} en {} ms", url, response.statusCode(), ms);
            throw new ExaException(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Petición a Exa {} interrumpida", url, e);
            throw new ExaException(0, "Petición interrumpida.");
        }
    }
}