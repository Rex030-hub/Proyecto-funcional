package com.bibliography.analyzer.service;

import com.bibliography.analyzer.dominio.url.ApaFormatter;
import com.bibliography.analyzer.dominio.url.ScoreCalculator;
import com.bibliography.analyzer.dominio.url.WebMetadata;
import com.bibliography.analyzer.dto.FuenteAnalisis;
import com.bibliography.analyzer.dto.FuenteDatos;
import com.bibliography.analyzer.repository.FuenteRepository;
import com.bibliography.analyzer.repository.FuenteRepository.FuenteRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

@Service
@ConditionalOnProperty(name = "app.modulo.url.enabled", havingValue = "true", matchIfMissing = true)
public class UrlAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(UrlAnalysisService.class);
    private static final int MAX_RELACIONADAS = 5;
    /** Máximo 3 análisis URL simultáneos: protege hilos y créditos de Exa. */
    private static final Semaphore SLOTS_URL = new Semaphore(3);

    private final ExaClient exa;
    private final FuenteRepository repo;

    public UrlAnalysisService(ExaClient exa, FuenteRepository repo) {
        this.exa = exa;
        this.repo = repo;
    }

    public FuenteAnalisis analizar(String url) throws IOException {
        if (!SLOTS_URL.tryAcquire()) {
            throw new ServiceSaturedException("de análisis URL");
        }
        try {
            if (url == null || url.isBlank()) throw new IllegalArgumentException("La URL está vacía.");
            URI uri;
            try {
                uri = new URI(url.trim());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("La URL no tiene un formato válido.");
            }
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
            if (uri.getHost() == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                throw new IllegalArgumentException("La URL debe usar esquema http o https y tener un dominio.");
            }
            long t0 = System.currentTimeMillis();
            String normal = url.trim();

            long tCache = System.currentTimeMillis();
            boolean existe = repo.exists(normal);
            log.info("[url] consulta de caché para {} en {} ms -> {}",
                    normal, System.currentTimeMillis() - tCache, existe ? "hit" : "miss");

            if (existe) {
                FuenteRow row = repo.findByUrl(normal)
                        .orElseThrow(() -> new IllegalStateException("Inconsistencia: exists() verdadero sin registro."));
                long tRel = System.currentTimeMillis();
                List<FuenteDatos> relacionadas = relacionadas(row);
                long ms = System.currentTimeMillis() - t0;
                log.info("[url] relacionadas resueltas en {} ms; total servido desde cache {} ms",
                        System.currentTimeMillis() - tRel, ms);
                return new FuenteAnalisis(toDatos(row), true, relacionadas, ms);
            }

            long tExa = System.currentTimeMillis();
            WebMetadata meta = exa.extractData(normal);
            log.info("[url] extracción Exa de {} completada en {} ms", normal, System.currentTimeMillis() - tExa);

            long tSave = System.currentTimeMillis();
            repo.save(meta);
            log.info("[url] fuente persistida en {} ms", System.currentTimeMillis() - tSave);

            FuenteRow row = repo.findByUrl(normal)
                    .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la fuente recién guardada."));
            long tRel = System.currentTimeMillis();
            List<FuenteDatos> relacionadas = relacionadas(row);
            long ms = System.currentTimeMillis() - t0;
            log.info("[url] relacionadas resueltas en {} ms; total análisis {} ms",
                    System.currentTimeMillis() - tRel, ms);
            return new FuenteAnalisis(toDatos(row), false, relacionadas, ms);
        } finally {
            SLOTS_URL.release();
        }
    }

    private List<FuenteDatos> relacionadas(FuenteRow row) throws IOException {
        if (repo.hasMatchingTopicCount(row.topic(), row.url(), MAX_RELACIONADAS)) {
            log.info("[url] similares de '{}' servidas desde BD", row.topic());
            return repo.similaresPorTopic(row.topic(), row.url(), MAX_RELACIONADAS).stream()
                    .map(this::toDatos).toList();
        }
        List<String> urls = exa.findSimilarPagesByTopic(row.topic(), MAX_RELACIONADAS);
        log.info("[url] Exa devolvió {} URLs similares para '{}'", urls.size(), row.topic());
        List<FuenteDatos> out = new ArrayList<>();
        for (String u : urls) {
            Optional<FuenteRow> existente = repo.findByUrl(u);
            FuenteRow r = existente.orElseGet(() -> {
                try {
                    repo.save(exa.extractData(u));
                    return repo.findByUrl(u).orElse(null);
                } catch (IOException e) {
                    log.warn("[url] similar {} no pudo extraerse: {}", u, e.getMessage());
                    return null;
                }
            });
            if (r != null) out.add(toDatos(r));
        }
        return out;
    }

    public FuenteDatos toDatos(FuenteRow r) {
        WebMetadata m = new WebMetadata(r.domain(), r.url(), r.title(), r.topic(),
                r.author(), r.publishedDate(), r.wordCount(), r.score());
        return new FuenteDatos(
                r.id(), r.url(), r.domain(), r.title(), r.author(), r.publishedDate(), r.topic(),
                r.score(),               // puntaje PERSISTIDO: ya no se recalcula en lectura
                ApaFormatter.format(m),  // APA RECALCULADO: solo necesita metadatos guardados
                r.wordCount(),
                r.createdAt());
    }

    public List<FuenteDatos> listar() {
        return repo.findAll().stream().map(this::toDatos).toList();
    }

    public Optional<FuenteDatos> porId(long id) {
        return repo.findById(id).map(this::toDatos);
    }

    public Optional<FuenteDatos> actualizar(long id, String titulo, String autor, String topico) {
        if (!repo.update(id, titulo, autor, topico)) return Optional.empty();
        return repo.findById(id).map(this::toDatos);
    }

    public boolean eliminar(long id) {
        return repo.deleteById(id);
    }
}