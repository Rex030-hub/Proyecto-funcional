package com.bibliography.analyzer.analisis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Estructura de datos inmutable: se computa UNA vez por documento y se comparte entre pilares. */
public final class TextoProcesado {
    private final String texto;
    private final String lower;
    private final String[] palabras;
    private final List<String> oraciones;
    private final List<String> parrafos;
    private final Map<String, Integer> bigramas;

    public TextoProcesado(String textoLimpio, List<String> parrafos) {
        this.texto = textoLimpio;
        this.lower = textoLimpio.toLowerCase();
        String normalized = textoLimpio.trim();
        this.palabras = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
        this.oraciones = dividirOraciones(textoLimpio);
        this.parrafos = parrafos;
        this.bigramas = construirBigramas();
    }

    public String texto() { return texto; }
    public String lower() { return lower; }
    public List<String> oraciones() { return oraciones; }
    public List<String> parrafos() { return parrafos; }
    public Map<String, Integer> bigramas() { return bigramas; }
    public int totalPalabras() { return palabras.length; }

    private List<String> dividirOraciones(String t) {
        List<String> out = new ArrayList<>();
        for (String o : t.split("(?<=[.!?])\\s+")) {
            String s = o.trim();
            int n = s.isEmpty() ? 0 : s.split("\\s+").length;
            if (n > 3 && n < 120) out.add(s);
        }
        return out;
    }

    private Map<String, Integer> construirBigramas() {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i + 1 < palabras.length; i++) {
            if (!Diccionarios.STOP_WORDS.contains(palabras[i]) && !Diccionarios.STOP_WORDS.contains(palabras[i + 1])) {
                m.merge(palabras[i] + " " + palabras[i + 1], 1, Integer::sum);
            }
        }
        return m;
    }
}