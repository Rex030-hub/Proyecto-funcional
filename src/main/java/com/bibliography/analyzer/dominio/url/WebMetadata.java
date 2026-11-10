package com.bibliography.analyzer.dominio.url;

public record WebMetadata(
        String domain,
        String url,
        String title,
        String topic,
        String author,
        String publishedDate,
        String textContent) {
}