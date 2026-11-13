package com.bibliography.analyzer.dominio.url;
import java.time.LocalDate;
import java.time.Period;
import java.lang.Math;

public final class ScoreCalculator {
    private ScoreCalculator() {}

    private static final int[] PESOS = {30, 30, 20, 15, 5}; // dominio, autor, fecha, extensión, referencias

    public static int calcular(String domain, String author, String publishedDate, String textContent) {
        String d = domain == null ? "" : domain.toLowerCase();
        String t = textContent == null ? "" : textContent.toLowerCase();
        int[] s = {
            evaluarDominio(d),
            evaluarAutor(author),
            evaluarFecha(publishedDate),
            evaluarLongitud(t),
            evaluarReferencias(t)
        };
        int total = 0;
        for (int i = 0; i < s.length; i++) total += (s[i] * PESOS[i]) / 100;
        return Math.min(total, 100);
    }

    public static int evaluarDominio(String domain) {
        if (domain.isEmpty()) return 0;
        if (domain.endsWith(".edu") || domain.endsWith(".gov") || domain.endsWith(".gob")
                || domain.contains(".edu.") || domain.contains(".gov.") || domain.contains(".gob.")) return 100;
        if (domain.endsWith(".org")) return 50;
        return 25;
    }

    public static int evaluarAutor(String author) {
        if (author != null && !author.isEmpty() && !author.equalsIgnoreCase("Sin autor")) return 100;
        return 25;
    }

    public static int evaluarFecha(String publishedDate) {
        if (publishedDate == null || publishedDate.isEmpty() 
                || publishedDate.equalsIgnoreCase("Fecha no especificada")) {
            return 25;
        }
        
        try {
            // Intentar formato ISO completo primero (2024-05-01T00:00:00.000Z)
            String normalized = publishedDate.trim();
            if (normalized.contains("T")) {
                // Extraer solo la parte de fecha antes de la T
                normalized = normalized.split("T")[0];
            }
            
            // Intentar parsear como YYYY-MM-DD
            LocalDate datePublished = LocalDate.parse(normalized);
            LocalDate today = LocalDate.now();
            
            // CORRECCIÓN: el Period debe ir de pasada a hoy, no al revés
            int years = Period.between(datePublished, today).getYears();
            int porcent = 70;
            double score = 100 - Math.max(0, Math.min(100, years * porcent / 100));
            return (int) score;
            
        } catch (Exception e) {
            // Fecha mal formateada: devolver puntaje neutral
            return 25;
        }
    }

    public static int evaluarLongitud(String textContent) {
        int length = textContent.length();
        if (length > 5000) return 100;
        if (length > 1500) return 50;
        if (length > 500) return 25;
        return 0;
    }

    public static int evaluarReferencias(String textContent) {
        boolean hasReferences = textContent.contains("referencia")
                || textContent.contains("bibliograf")
                || textContent.contains("citation")
                || textContent.contains("references")
                || textContent.contains("doi:")
                || textContent.contains("doi.org");
        return hasReferences ? 100 : 0;
    }
}