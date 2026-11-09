package com.bibliography.analyzer.controller;

import com.bibliography.analyzer.dto.AnalisisRespuesta;
import com.bibliography.analyzer.dto.DatosAnalisis;
import com.bibliography.analyzer.service.AnalisisService;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DocumentoController {

    private static final long MAX_BYTES = 25L * 1024 * 1024;

    @Autowired
    private AnalisisService analisisService;

    @PostMapping("/analizar-documento")
    public ResponseEntity<AnalisisRespuesta> analizarDocumento(@RequestParam("documento") MultipartFile archivo) {
        try {
            if (archivo == null || archivo.isEmpty()|| archivo.getSize() == 0) {
                return ResponseEntity.status(400).body(new AnalisisRespuesta(false, null, "El archivo está vacío."));
            }
            if (archivo.getSize() > MAX_BYTES) {
                return ResponseEntity.status(413).body(new AnalisisRespuesta(false, null, "Archivo demasiado grande: el límite es 25 MB."));
            }
            byte[] head = new byte[5];
            try (InputStream is = archivo.getInputStream()) {
                if (is.read(head) < 5 || !new String(head, StandardCharsets.US_ASCII).startsWith("%PDF-")) {
                    return ResponseEntity.status(415).body(new AnalisisRespuesta(false, null, "El archivo no es un PDF válido: su firma binaria no coincide con la de un PDF."));
                }
            }
            DatosAnalisis datos = analisisService.analizarDocumento(archivo);
            return ResponseEntity.ok(new AnalisisRespuesta(true, datos, null));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(423).body(new AnalisisRespuesta(false, null, "El PDF está protegido con contraseña y no puede analizarse sin ella."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(422).body(new AnalisisRespuesta(false, null, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(422).body(new AnalisisRespuesta(false, null, "El PDF está dañado o no pudo leerse correctamente."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new AnalisisRespuesta(false, null, "Error interno del servidor durante el análisis."));
        }
    }
}