package com.bibliography.analyzer.dominio.url;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScoreCalculator {
    private ScoreCalculator() {}

    private static final int[] PESOS = {25, 25, 15, 15, 15, 5}; // dominio, autor, fecha, extensión, Signos de puntuación, referencias

    public static int calcular(String domain, String author, String publishedDate, String textContent) {
        String d = domain == null ? "" : domain.toLowerCase();
        String t = textContent == null ? "" : textContent.toLowerCase();
        int[] s = {
            evaluarDominio(d),
            evaluarAutor(author),
            evaluarFecha(publishedDate),
            evaluarLongitud(t),
            evaluarReferencias(t),
            evaluarPuntuacion(textContent)
        };
        int total = 0;
        for (int i = 0; i < s.length; i++) total += (s[i] * PESOS[i]) / 100;
        return Math.min(total, 100);
    }

    public static int evaluarDominio(String domain) {
        if (domain.isEmpty()) return 0;
        if (domain.endsWith(".edu") || domain.endsWith(".gov") || domain.endsWith(".gob")
                || domain.contains(".edu.") || domain.contains(".gov.") || domain.contains(".gob.")) return 100;
        if (domain.endsWith(".org")) return 50;
        return 25;
    }

    public static int evaluarAutor(String author) {
        if (author != null && !author.isEmpty() && !author.equalsIgnoreCase("Sin autor")) return 100;
        return 25;
    }

    public static int evaluarFecha(String publishedDate) {
        if (publishedDate == null || publishedDate.isEmpty() 
                || publishedDate.equalsIgnoreCase("Fecha no especificada")) {
            return 25;
        }
        
        try {
            // Intentar formato ISO completo primero (2024-05-01T00:00:00.000Z)
            String normalized = publishedDate.trim();
            if (normalized.contains("T")) {
                // Extraer solo la parte de fecha antes de la T
                normalized = normalized.split("T")[0];
            }
            
            // Intentar parsear como YYYY-MM-DD
            LocalDate datePublished = LocalDate.parse(normalized);
            LocalDate today = LocalDate.now();
            
            // CORRECCIÓN: el Period debe ir de pasada a hoy, no al revés
            int years = Period.between(datePublished, today).getYears();
            int porcent = 70;
            double score = 100 - Math.max(0, Math.min(100, years * porcent / 100));
            return (int) score;
            
        } catch (Exception e) {
            // Fecha mal formateada: devolver puntaje neutral
            return 25;
        }
    }

    public static int evaluarLongitud(String textContent) {
        int length = textContent.length();
        if (length > 5000) return 100;
        if (length > 1500) return 50;
        if (length > 500) return 25;
        return 0;
    }

    public static int evaluarReferencias(String textContent) {
        boolean hasReferences = textContent.contains("referencia")
                || textContent.contains("bibliograf")
                || textContent.contains("citation")
                || textContent.contains("references")
                || textContent.contains("doi:")
                || textContent.contains("doi.org");
        return hasReferences ? 100 : 0;
    }

    public static int evaluarPuntuacion(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0;
        }

        String textoLimpio = texto.trim();
        double puntaje = 100.0;
        List<String> erroresDetectados = new ArrayList<>(); // Útil para depurar

        // 1. ESPACIO ANTES DEL SIGNO (Ej: "hola , mundo" o "fin .") -> -5 pts por error
        Matcher espacioAntes = Pattern.compile("\\s+[.,;:]").matcher(textoLimpio);
        while (espacioAntes.find()) {
            puntaje -= 5;
            erroresDetectados.add("Espacio antes de signo: '" + espacioAntes.group() + "'");
        }

        // 2. FALTA DE ESPACIO DESPUÉS DEL SIGNO (Ej: "hola,mundo" o "fin.Inicio") -> -5 pts por error
        // Se ignoran números decimales (3.14) y horas (14:30)
        Matcher sinEspacioDespues = Pattern.compile("(?<!\\d)[.,;:](?![\\s\\d.\"”’)]|$)").matcher(textoLimpio);
        while (sinEspacioDespues.find()) {
            puntaje -= 5;
            erroresDetectados.add("Falta espacio después de: '" + sinEspacioDespues.group() + "'");
        }

        // 3. COMBINACIONES INVÁLIDAS O REPETIDAS (Ej: ",," o ".;" o "..") -> -8 pts por error
        // Permite exactamente tres puntos seguidos (...)
        Matcher signosRepetidos = Pattern.compile("([,;:])\\1+|(?<!\\.)\\.{2}(?!\\.)|\\.{4,}|[.,;:][,;:]").matcher(textoLimpio);
        while (signosRepetidos.find()) {
            puntaje -= 8;
            erroresDetectados.add("Uso inválido/repetido de signos: '" + signosRepetidos.group() + "'");
        }

        // 4. MAYÚSCULA DESPUÉS DE PUNTO -> -6 pts por error
        // Busca un punto seguido de espacios y una letra minúscula
        Matcher minusculaTrasPunto = Pattern.compile("(?<!\\.)\\.\\s+[a-záéíóúñ]").matcher(textoLimpio);
        while (minusculaTrasPunto.find()) {
            puntaje -= 6;
            erroresDetectados.add("Minúscula después de punto: '" + minusculaTrasPunto.group() + "'");
        }

        // 5. MAYÚSCULA INJUSTIFICADA DESPUÉS DE PUNTO Y COMA -> -4 pts por error
        Matcher mayusculaTrasPuntoYComa = Pattern.compile(";\\s+[A-ZÁÉÍÓÚÑ]").matcher(textoLimpio);
        while (mayusculaTrasPuntoYComa.find()) {
            puntaje -= 4;
            erroresDetectados.add("Mayúscula después de punto y coma: '" + mayusculaTrasPuntoYComa.group() + "'");
        }

        // 6. CIERRE DEL TEXTO -> -10 pts si no termina en punto (o puntos suspensivos)
        if (!textoLimpio.endsWith(".")) {
            puntaje -= 10;
            erroresDetectados.add("El texto no termina con punto.");
        }

        // 7. AUSENCIA DE PAUSAS (Muro sin puntuación) -> -10 pts por cada tramo de +30 palabras sin signos
        String[] bloquesSinPuntuacion = textoLimpio.split("[.,;:]");
        for (String bloque : bloquesSinPuntuacion) {
            String[] palabras = bloque.trim().split("\\s+");
            if (palabras.length > 30) {
                puntaje -= 10;
                erroresDetectados.add("Tramo muy largo sin puntuación (" + palabras.length + " palabras).");
            }
        }

        // (Opcional) Imprimir errores en consola para verificar qué descontó puntos
        // erroresDetectados.forEach(System.out::println);
        return (int) Math.max(0, Math.round(puntaje));
    }
}