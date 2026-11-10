package com.bibliography.analyzer.dominio.url;

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
        String formattedDate = "(s. f.). ";
        String date = m.publishedDate();
        if (date != null && !date.isBlank()
                && !date.equalsIgnoreCase("Fecha no especificada") && !date.equalsIgnoreCase("n.d.")) {
            formattedDate = "(" + date.trim() + "). ";
        }
        String title = (m.title() != null && !m.title().isBlank()) ? m.title().trim() : "Sin título";
        String domain = (m.domain() != null && !m.domain().isBlank()) ? m.domain().trim() : "";
        String url = (m.url() != null) ? m.url().trim() : "";
        if (!formattedAuthor.isEmpty()) {
            return String.format("%s%s%s. %s. %s", formattedAuthor, formattedDate, title, domain, url);
        }
        return String.format("%s. %s%s. %s", title, formattedDate, domain, url);
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