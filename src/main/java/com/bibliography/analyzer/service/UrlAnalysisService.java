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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UrlAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(UrlAnalysisService.class);
    private static final int MAX_RELACIONADAS = 5;

    private final ExaClient exa;
    private final FuenteRepository repo;

    public UrlAnalysisService(ExaClient exa, FuenteRepository repo) {
        this.exa = exa;
        this.repo = repo;
    }

    public FuenteAnalisis analizar(String url) throws IOException {
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

        if (repo.exists(normal)) {
            FuenteRow row = repo.findByUrl(normal)
                    .orElseThrow(() -> new IllegalStateException("Inconsistencia: exists() verdadero sin registro."));
            List<FuenteDatos> relacionadas = relacionadas(row);
            long ms = System.currentTimeMillis() - t0;
            log.info("[url] {} servida desde base de datos en {} ms", normal, ms);
            return new FuenteAnalisis(toDatos(row), true, relacionadas, ms);
        }

        WebMetadata meta = exa.extractData(normal);
        repo.save(meta);
        FuenteRow row = repo.findByUrl(normal)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la fuente recién guardada."));
        List<FuenteDatos> relacionadas = relacionadas(row);
        long ms = System.currentTimeMillis() - t0;
        log.info("[url] {} analizada vía Exa y persistida en {} ms", normal, ms);
        return new FuenteAnalisis(toDatos(row), false, relacionadas, ms);
    }

    private List<FuenteDatos> relacionadas(FuenteRow row) throws IOException {
        if (repo.hasMatchingTopicCount(row.topic(), row.url(), MAX_RELACIONADAS)) {
            return repo.similaresPorTopic(row.topic(), row.url(), MAX_RELACIONADAS).stream()
                    .map(this::toDatos).toList();
        }
        List<String> urls = exa.findSimilarPagesByTopic(row.topic(), MAX_RELACIONADAS);
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
                r.author(), r.publishedDate(), r.textContent());
        return new FuenteDatos(
                r.id(), r.url(), r.domain(), r.title(), r.author(), r.publishedDate(), r.topic(),
                ScoreCalculator.calcular(r.domain(), r.author(), r.publishedDate(), r.textContent()),
                ApaFormatter.format(m), r.createdAt());
    }

    // ===== CRUD para el controller =====
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