package com.bibliography.analyzer.controller;

import com.bibliography.analyzer.dto.FuenteDatos;
import com.bibliography.analyzer.dto.RespuestaApi;
import com.bibliography.analyzer.dto.UrlRequest;
import com.bibliography.analyzer.dto.UrlUpdateRequest;
import com.bibliography.analyzer.repository.FuenteRepository;
import com.bibliography.analyzer.service.ExaClient;
import com.bibliography.analyzer.service.UrlAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import java.net.http.HttpTimeoutException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "app.modulo.url.enabled", havingValue = "true", matchIfMissing = true) 
public class FuenteController {

    private final UrlAnalysisService service;
    private static final Logger log = LoggerFactory.getLogger(FuenteController.class);
    private static final String MENSAJE_INTERNO =
            "Error interno del servidor. Intenta nuevamente en unos minutos.";

    public FuenteController(UrlAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analizar-url")
    public ResponseEntity<RespuestaApi> analizarUrl(@RequestBody UrlRequest request) {
        try {
            return ResponseEntity.ok(RespuestaApi.ok(service.analizar(request == null ? null : request.url())));
        } catch (IllegalArgumentException e) {
            // Error del usuario: mensaje específico OK
            return ResponseEntity.status(400).body(RespuestaApi.error(e.getMessage()));
        } catch (HttpTimeoutException e) {
            log.error("Timeout consultando el motor de búsqueda externo", e);
            return ResponseEntity.status(504).body(RespuestaApi.error(
                    "Tiempo de espera agotado al consultar el motor de búsqueda externo."));
        } catch (ExaClient.ExaException e) {
            log.error("El motor de búsqueda externo falló: HTTP {}", e.statusCode(), e);
            return ResponseEntity.status(502).body(RespuestaApi.error(
                    "El motor de búsqueda externo no está disponible en este momento."));
        } catch (FuenteRepository.DataBaseException e) {
            log.error("Error de base de datos en analizar-url", e);
            return ResponseEntity.status(500).body(RespuestaApi.error(MENSAJE_INTERNO));
        } catch (IllegalStateException e) {
            log.error("Configuración interna inválida (claves/estado)", e);
            return ResponseEntity.status(500).body(RespuestaApi.error(MENSAJE_INTERNO));
        } catch (Exception e) {
            log.error("Error no controlado en analizar-url", e);
            return ResponseEntity.status(502).body(RespuestaApi.error(MENSAJE_INTERNO));
        }
    }

    @GetMapping("/fuentes")
    public ResponseEntity<RespuestaApi> listar() {
        return ResponseEntity.ok(RespuestaApi.ok(service.listar()));
    }

    @GetMapping("/fuentes/{id}")
    public ResponseEntity<RespuestaApi> porId(@PathVariable long id) {
        Optional<FuenteDatos> dato = service.porId(id);
        if (dato.isEmpty()) {
            return ResponseEntity.status(404).body(RespuestaApi.error("No existe una fuente con id " + id + "."));
        }
        return ResponseEntity.ok(RespuestaApi.ok(dato.get()));
    }

    @PutMapping("/fuentes/{id}")
    public ResponseEntity<RespuestaApi> actualizar(@PathVariable long id, @RequestBody UrlUpdateRequest request) {
        Optional<FuenteDatos> actualizada = service.actualizar(id,
                request == null ? null : request.titulo(),
                request == null ? null : request.autor(),
                request == null ? null : request.topico());
        if (actualizada.isEmpty()) {
            return ResponseEntity.status(404).body(RespuestaApi.error("No existe una fuente con id " + id + "."));
        }
        return ResponseEntity.ok(RespuestaApi.ok(actualizada.get()));
    }

    @DeleteMapping("/fuentes/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        if (!service.eliminar(id)) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.noContent().build();
    }
}