package com.bibliography.analyzer.analisis;

public record Metrica(String nombre, double score, boolean confiable, double valorCrudo, double peso, boolean puerta) {

    public Metrica(String nombre, double score, boolean confiable, double valorCrudo, double peso) {
        this(nombre, score, confiable, valorCrudo, peso, false);
    }

    public boolean votaIA()     { return confiable && score >= 65; }
    public boolean votaHumano() { return confiable && score <= 35; }
}