package com.bibliography.analyzer.controller;

import com.bibliography.analyzer.dto.RespuestaApi;
import com.bibliography.analyzer.service.ServiceSaturedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException; // <-- CORREGIDO: Parameter en vez de Part
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RespuestaApi> tooLarge(MaxUploadSizeExceededException e) {
        log.error("Archivo supera el límite de subida: {}", e.getMessage());
        return ResponseEntity.status(413).body(RespuestaApi.error("Archivo demasiado grande: el límite es 25 MB."));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<RespuestaApi> badRequest(Exception e) {
        log.warn("Petición inválida (400): {}", e.getMessage());
        return ResponseEntity.status(400).body(RespuestaApi.error("Petición inválida: revise el formato o los campos enviados (ej. falta el archivo PDF o el JSON está mal formado)."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaApi> methodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Método no soportado (405): {}", e.getMessage());
        return ResponseEntity.status(405).body(RespuestaApi.error("Método HTTP no soportado para este recurso."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaApi> notFound(NoResourceFoundException e) {
        log.warn("Recurso no encontrado (404): {}", e.getMessage());
        return ResponseEntity.status(404).body(RespuestaApi.error("Recurso no encontrado o módulo no habilitado en este servicio."));
    }

    @ExceptionHandler(ServiceSaturedException.class)
    public ResponseEntity<RespuestaApi> saturated(ServiceSaturedException e) {
        log.warn("Saturación de servicio (503): {}", e.getMessage());
        return ResponseEntity.status(503).body(RespuestaApi.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaApi> internal(Exception e) {
        log.error("Error interno no controlado (500)", e);
        return ResponseEntity.status(500).body(
                RespuestaApi.error("Error interno del servidor. Identifique el incidente con el header X-Request-Id en los logs."));
    }
}