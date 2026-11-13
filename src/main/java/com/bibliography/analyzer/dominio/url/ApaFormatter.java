package com.bibliography.analyzer.dominio.url;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class ApaFormatter {
    private ApaFormatter() {}

    public static String format(WebMetadata m) {
        if (m == null) return "";
        
        String formattedAuthor = "";
        String author = m.author();
        if (author != null && !author.isBlank()
                && !author.equalsIgnoreCase("Sin autor") && !author.equalsIgnoreCase("Unknown")) {
            formattedAuthor = formatAuthorName(author) + " ";
        }
        
        String formattedDate = formatApaDate(m.publishedDate());
        
        String title = (m.title() != null && !m.title().isBlank()) ? m.title().trim() : "Sin título";
        String domain = (m.domain() != null && !m.domain().isBlank()) ? m.domain().trim() : "";
        String url = (m.url() != null) ? m.url().trim() : "";
        
        if (!formattedAuthor.isEmpty()) {
            return String.format("%s%s%s. %s. %s", formattedAuthor, formattedDate, title, domain, url);
        }
        return String.format("%s. %s%s. %s", title, formattedDate, domain, url);
    }

    private static String formatApaDate(String publishedDate) {
        if (publishedDate == null || publishedDate.isBlank()
                || publishedDate.equalsIgnoreCase("Fecha no especificada")
                || publishedDate.equalsIgnoreCase("n.d.")) {
            return "(s. f.). ";
        }
        
        try {
            String normalized = publishedDate.trim();
            
            // Si tiene formato ISO completo (2025-06-28T22:18:00.000Z), extraer solo la fecha
            if (normalized.contains("T")) {
                normalized = normalized.split("T")[0];
            }
            
            LocalDate date = LocalDate.parse(normalized);
            
            // Formato APA 7: (2025, junio 28).
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy, MMMM d", new Locale("es", "ES"));
            return "(" + date.format(formatter) + "). ";
            
        } catch (DateTimeParseException e) {
            // Si no se puede parsear, intentar extraer solo el año
            try {
                if (publishedDate.matches("\\d{4}.*")) {
                    String year = publishedDate.substring(0, 4);
                    return "(" + year + "). ";
                }
            } catch (Exception ex) {
                // Ignorar y devolver formato por defecto
            }
            return "(s. f.). ";
        }
    }

    private static String formatAuthorName(String rawAuthor) {
        String trimmed = rawAuthor.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length >= 2) {
            return parts[parts.length - 1] + ", " + parts[0].substring(0, 1).toUpperCase() + ".";
        }
        return trimmed; // organizaciones se retornan tal cual
    }
}