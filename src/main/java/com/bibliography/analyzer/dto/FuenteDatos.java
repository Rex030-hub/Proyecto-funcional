package com.bibliography.analyzer.dto;
public record FuenteDatos(Long id, String url, String dominio, String titulo, String autor,
        String fechaPublicacion, String topico, int puntajeConfianza,
        String formatoApa, int cantidadPalabras, String fechaAnalisis) {}