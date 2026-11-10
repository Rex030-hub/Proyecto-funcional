package com.bibliography.analyzer.controller;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final Environment env;

    public HealthController(Environment env) {
        this.env = env;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("servicio", "analyzer-backend");
        body.put("moduloPdf", env.getProperty("app.modulo.pdf.enabled", "true"));
        body.put("moduloUrl", env.getProperty("app.modulo.url.enabled", "true"));
        return ResponseEntity.ok(body);
    }
}