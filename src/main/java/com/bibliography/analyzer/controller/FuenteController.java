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

import java.net.http.HttpTimeoutException;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FuenteController {

    private final UrlAnalysisService service;

    public FuenteController(UrlAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analizar-url")
    public ResponseEntity<RespuestaApi> analizarUrl(@RequestBody UrlRequest request) {
        try {
            return ResponseEntity.ok(RespuestaApi.ok(service.analizar(request == null ? null : request.url())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(RespuestaApi.error(e.getMessage()));
        } catch (HttpTimeoutException e) {
            return ResponseEntity.status(504).body(RespuestaApi.error(
                    "Tiempo de espera agotado al consultar el motor de búsqueda externo."));
        } catch (ExaClient.ExaException e) {
            return ResponseEntity.status(502).body(RespuestaApi.error(e.getMessage()));
        } catch (FuenteRepository.DataBaseException e) {
            return ResponseEntity.status(500).body(RespuestaApi.error("Error de base de datos: " + e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(RespuestaApi.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(RespuestaApi.error(
                    "No se pudo contactar al motor de búsqueda externo o la red falló durante el análisis."));
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