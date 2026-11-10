package com.bibliography.analyzer.analisis;

import java.util.Set;

public final class Idioma {
    private Idioma() {}

    private static final Set<String> FUNC_ES = Set.of(
            "de", "la", "el", "los", "las", "un", "una", "unos", "unas", "que", "se", "no",
            "es", "son", "en", "y", "a", "por", "con", "para", "su", "sus", "al", "lo",
            "como", "más", "pero", "del", "entre", "sobre", "hasta", "desde", "este", "esta",
            "estos", "estas", "porque", "cuando", "muy", "sin");

    // Palabras cortas REALES del español: no cuentan como fragmentos
    private static final Set<String> CORTAS_REALES = Set.of(
            "a", "y", "e", "o", "u", "de", "la", "el", "en", "un", "al", "lo", "se", "su",
            "si", "no", "es", "ya", "yo", "tu", "mi", "le", "te", "me", "ni", "da", "ir",
            "va", "ve", "ha", "he", "so", "os", "ui");

    /** true si el texto es mayoritariamente español funcional. */
    public static boolean esEspanol(String texto) {
        String[] toks = texto.toLowerCase().split("\\s+");
        if (toks.length < 50) return true;
        int hit = 0;
        for (String t : toks) {
            if (FUNC_ES.contains(t.replaceAll("[^a-záéíóúñü]", ""))) hit++;
        }
        return (double) hit / toks.length >= 0.25;
    }

    /** H16: detecta extracción fragmentada (tokens de ≤2 letras que no son palabras reales). */
    public static boolean extraccionFragmentada(String texto) {
        String[] toks = texto.toLowerCase().split("\\s+");
        if (toks.length < 100) return false;
        int frag = 0;
        for (String t : toks) {
            String clean = t.replaceAll("[^a-záéíóúñü]", "");
            if (!clean.isEmpty() && clean.length() <= 2 && !CORTAS_REALES.contains(clean)) frag++;
        }
        return (double) frag / toks.length >= 0.08;
    }
}