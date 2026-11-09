package com.bibliography.analyzer.analisis;

import com.bibliography.analyzer.dto.DatosAnalisis;
import com.bibliography.analyzer.dto.Estadistica;
import com.bibliography.analyzer.dto.Evidencia;

import java.util.ArrayList;
import java.util.List;

public final class ReporteForense {
    private ReporteForense() {}

    public static DatosAnalisis construir(String nombre, int indice, List<Metrica> votantes,
                                          List<Metrica> testigos, HuellaDigital.Resultado hd) {
        List<Estadistica> stats = new ArrayList<>();
        for (Metrica m : votantes) stats.add(new Estadistica(m.nombre() + " (/100p o según métrica)",
                fmt(m.valorCrudo()) + " · " + nivel(m)));
        for (Metrica m : testigos) if (m.confiable()) stats.add(new Estadistica(m.nombre() + " (testigo)",
                fmt(m.valorCrudo()) + " · " + nivel(m)));
        stats.addAll(hd.stats());

        List<String> indicadores = new ArrayList<>();
        List<Evidencia> evidencias = new ArrayList<>();

        for (Metrica m : votantes) {
            String[] frases = frasesDe(m.nombre());
            if (m.votaIA()) {
                indicadores.add(frases[0]);
                evidencias.add(new Evidencia("Señal de IA", m.nombre(), textoEvidencia(m), frases[2]));
            } else if (m.votaHumano()) {
                indicadores.add(frases[1]);
                evidencias.add(new Evidencia("Señal humana", m.nombre(), textoEvidencia(m), frases[3]));
            }
            if (m.puerta()) {
                evidencias.add(new Evidencia("Señal de IA", "Concentración sospechosa de marcadores",
                        m.nombre() + ": los marcadores aparecen en muy pocos párrafos del documento.",
                        "Los marcadores están concentrados en un único bloque en lugar de estar tejidos a lo largo del texto: patrón compatible con una inserción post-hoc de datos o de voz."));
            }
        }
        for (Metrica m : testigos) {
            if (m.nombre().equals("Repetición de bigramas") && m.confiable() && m.valorCrudo() >= 0.12) {
                indicadores.add("Repetición de términos técnicos propia de un dominio especializado (señal humana).");
            }
            if (m.nombre().equals("Uniformidad de párrafos") && m.confiable()) {
                if (m.score() >= 65) indicadores.add("Párrafos artificialmente simétricos.");
                else if (m.score() <= 35) indicadores.add("Párrafos de extensión orgánica y desigual.");
            }
        }
        evidencias.addAll(hd.evidencias());
        if (evidencias.size() > 7) evidencias = new ArrayList<>(evidencias.subList(0, 7));
        if (indicadores.isEmpty()) indicadores.add("Sin señales dominantes: el documento no activa patrones fuertes en ningún pilar.");

        return new DatosAnalisis(nombre, indice + "%", interpretacion(indice), stats, indicadores, evidencias);
    }

    private static String nivel(Metrica m) {
        if (!m.confiable()) return "Sin datos suficientes";
        return m.score() <= 35 ? "Humano" : m.score() < 65 ? "Mixto" : "IA";
    }

    private static String textoEvidencia(Metrica m) {
        return m.nombre() + ": valor medido " + fmt(m.valorCrudo()) + " (score de sospecha " + Math.round(m.score()) + "/100).";
    }

    private static String[] frasesDe(String nombre) {
        return switch (nombre) {
            case "Anclaje empírico" -> new String[]{
                    "Escasez de datos concretos, fechas, cifras o citas verificables.",
                    "Fuerte anclaje empírico: cifras, fechas, citas y entidades nombradas.",
                    "La IA generaliza con 'diversos estudios' sin datos concretos; los humanos anclan sus argumentos en evidencia verificable.",
                    "La densidad de datos verificables es propia de investigación humana real."};
            case "Complejidad sintáctica" -> new String[]{
                    "Puntuación simple y predecible (baja complejidad sintáctica).",
                    "Puntuación compleja: incisos, dos puntos, paréntesis y citas (flujo de pensamiento humano).",
                    "Los LLMs producen puntuación estándar y escolar; la ausencia de incisos y paréntesis es una firma algorítmica.",
                    "El uso de incisos, paréntesis y citas refleja el flujo de pensamiento de un autor humano."};
            case "Burstiness" -> new String[]{
                    "Baja variación en longitud de oraciones (monotonía estructural).",
                    "Alta variación en longitud de oraciones (ritmo humano natural).",
                    "La IA tiende a oraciones de longitud uniforme; la desviación medida es baja.",
                    "La alternancia de oraciones cortas y largas es característica del ritmo humano."};
            case "Voz autoral" -> new String[]{
                    "Ausencia de voz autoral (tono enciclopédico impersonal).",
                    "Voz autoral presente: primera persona, preguntas retóricas y analogías propias.",
                    "Los LLMs mantienen un tono impersonal constante; no hay primera persona ni preguntas propias.",
                    "La presencia de primera persona, preguntas y analogías imprime una perspectiva humana verificable."};
            default -> new String[]{
                    "Presencia de intensificadores vacíos típicos de LLM.",
                    "Sin intensificadores vacíos: el énfasis se logra con datos, no con adverbios.",
                    "Adverbios como 'profundamente' o 'innegable' son relleno sofisticado sin aporte semántico.",
                    "El texto no abusa de intensificadores vacíos."};
        };
    }

    private static String fmt(double v) {
        return v >= 10 ? String.format("%.1f", v) : String.format("%.2f", v);
    }

    private static String interpretacion(int indice) {
        if (indice < Parametros.BANDA_HUMANO) return "El análisis forense de 8 pilares no encontró indicios significativos de generación automática. El texto muestra variación estructural, anclaje empírico y/o voz autoral compatibles con escritura humana. Este resultado es una estimación estadística, no una certificación de autoría.";
        if (indice < Parametros.BANDA_IA) return "Zona gris: el documento combina patrones humanos y automatizados. Podría tratarse de escritura humana asistida por IA, o de un estilo humano particularmente formal. Se recomienda revisión manual de las evidencias marcadas antes de cualquier conclusión.";
        if (indice < 85) return "El análisis detectó varios patrones compatibles con generación por IA: bajo anclaje empírico, puntuación predecible y/o ausencia de voz autoral. El resultado es una estimación estadística basada en 8 pilares forenses, no una prueba definitiva.";
        return "Múltiples pilares forenses y la huella digital del archivo coinciden en patrones altamente compatibles con generación por IA. Aun así, este sistema no sustituye el juicio humano: revise las evidencias citadas antes de tomar cualquier decisión académica.";
    }
}