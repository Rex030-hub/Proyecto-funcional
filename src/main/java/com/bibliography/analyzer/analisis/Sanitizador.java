package com.bibliography.analyzer.analisis;

public final class Sanitizador {
    private Sanitizador() {}

    /** Escapa HTML de toda cadena proveniente del exterior (metadata, nombres de archivo). */
    public static String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}