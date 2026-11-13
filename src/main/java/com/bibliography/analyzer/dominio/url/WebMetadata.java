package com.bibliography.analyzer.dominio.url;

/** Portador persistible. El texto completo de Exa NO viaja aquí:
 *  se consume dentro de ExaClient.extractData y solo deja derivados. */
public record WebMetadata(
        String domain,
        String url,
        String title,
        String topic,
        String author,
        String publishedDate,
        int wordCount,
        int score) {
}