package com.bibliography.analyzer.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class ExtractorPdf {
    
    // Record que contiene TODOS los valores extraídos (no referencias)
    public record MetadataExtraida(
            String producer,
            String author,
            Calendar creationDate,
            Calendar modificationDate
    ) {}
    
    public record Extraccion(String textoCrudo, MetadataExtraida metadata) {}
    
    private ExtractorPdf() {}

    public static Extraccion extraer(byte[] bytes) throws IOException {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper s = new PDFTextStripper();
            String texto = s.getText(doc);
            
            // Extraer metadata ANTES de que se cierre el documento
            PDDocumentInformation info = doc.getDocumentInformation();
            MetadataExtraida metadata = new MetadataExtraida(
                    info.getProducer() != null ? info.getProducer() : "",
                    info.getAuthor() != null ? info.getAuthor() : "",
                    info.getCreationDate(),
                    info.getModificationDate()
            );
            
            return new Extraccion(texto, metadata);
        }
    }

    public static String limpiar(String crudo) {
        return crudo
                .replaceAll("-\\s*\\n\\s*", "")
                .replaceAll("(?<!\\n)\\n(?!\\n)", " ")
                .replaceAll(" +", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    public static List<String> parrafos(String crudo) {
        String[] bloques = crudo.split("\\n\\s*\\n");
        List<String> candidatos = new ArrayList<>();
        if (bloques.length >= 4) {
            for (String b : bloques) candidatos.add(b);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String linea : crudo.split("\\n")) {
                String l = linea.trim();
                if (l.isEmpty()) {
                    if (sb.length() > 0) { candidatos.add(sb.toString()); sb.setLength(0); }
                    continue;
                }
                if (sb.length() > 0) sb.append(' ');
                sb.append(l);
                if (l.length() > 60 && l.matches(".*[.!?]$")) { candidatos.add(sb.toString()); sb.setLength(0); }
            }
            if (sb.length() > 0) candidatos.add(sb.toString());
        }
        List<String> out = new ArrayList<>();
        for (String c : candidatos) {
            String t = c.replaceAll("\\s+", " ").trim();
            if (t.length() > 80) out.add(t);
        }
        return out;
    }
}