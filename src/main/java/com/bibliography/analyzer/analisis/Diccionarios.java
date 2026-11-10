package com.bibliography.analyzer.analisis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Diccionarios {
    private Diccionarios() {}

    public static final Set<String> CONECTORES_VACIOS = Set.of(
            "cabe destacar", "cabe señalar", "cabe mencionar",
            "es importante mencionar", "es fundamental destacar", "resulta fundamental",
            "es interesante notar", "vale la pena mencionar", "cabe tener en cuenta",
            "en el panorama actual", "bajo esta perspectiva", "desde esta óptica",
            "es imperativo", "no cabe duda", "en síntesis", "en definitiva",
            "es fundamental", "resulta necesario", "imprescindible");

    public static final Set<String> INTENSIFICADORES = Set.of(
            "profundamente", "innegable", "innegablemente", "indudablemente",
            "evidentemente", "claramente", "holístico", "holística",
            "sinergia", "sinergias", "desentrañar", "panacea",
            "vertiginoso", "vertiginosa");

    public static final Set<String> VOZ_MARCADORES = Set.of(
            "yo", "nosotros", "nosotras", "mí", "mi", "mis",
            "nuestro", "nuestra", "nuestros", "nuestras",
            "concordamos", "sostenemos", "sostendremos", "queremos", "advertimos",
            "pensamos", "creemos", "observamos", "comprobamos",
            "entrevisté", "construí", "relaté", "me he",
            "hemos visto", "hemos observado", "mi experiencia", "nuestra experiencia",
            "debemos", "proponemos", "consideramos");

    public static final Set<String> ANALOGIAS = Set.of(
            "una analogía", "buena analogía", "es como", "como si",
            "semejante a", "imagina", "piensa en");

    public static final Set<String> STOP_WORDS = Set.of(
            "de", "la", "el", "en", "y", "a", "los", "del", "las", "un", "por", "con", "para",
            "su", "es", "al", "lo", "como", "más", "pero", "sus", "le", "ya", "o", "este", "sí",
            "porque", "esta", "son", "entre", "cuando", "muy", "sin", "sobre", "también",
            "hasta", "hay", "donde", "quien", "desde", "todo", "nos", "durante", "todos", "uno",
            "les", "ni", "contra", "otros", "ese", "eso", "ante", "ellos", "e", "esto",
            "antes", "algunos", "qué", "unos", "otro", "otras", "otra", "él", "tanto",
            "esa", "estos", "mucho", "quienes", "nada", "muchos", "cual", "poco", "ella",
            "estas", "algunas", "algo", "tus", "ellas",
            "os", "esos", "esas", "que", "se", "no", "si");

    public static final Set<String> NOMBRES_MODELO = Set.of(
            "claude", "chatgpt", "gpt", "gemini", "copilot", "llama", "qwen",
            "deepseek", "mistral", "anthropic", "openai");

    public static final Set<String> PRODUCTOR_EXPORT_DIRECTO = Set.of(
            "reportlab", "weasyprint", "pdf-lib", "pdflib", "wkhtmltopdf", "fpdf",
            "tcpdf", "dompdf", "pdfkit", "jasperreports", "skia", "chromium",
            "quartz", "microsoft word", "google docs", "libreoffice", "openoffice");

    public static final Set<String> PRODUCTOR_EDITORIAL = Set.of(
            "distiller", "adobe pdf library", "pdftex", "xetex", "luatex",
            "arbortext", "3b2", "antenna house");

    public static final Set<String> PRODUCTOR_CONVERSOR = Set.of(
            "ilovepdf", "smallpdf", "pdfcandy", "sejda", "pdf24", "sodipodi");

    // ===== Patterns precompilados UNA sola vez (rendimiento en concurrencia) =====
    public static final Pattern P_ANIOS = Pattern.compile("\\b(19|20)\\d{2}\\b");
    public static final Pattern P_NUM_UNIDAD = Pattern.compile(
            "\\d+(?:[.,]\\d+)?\\s*(?:%|por ciento|mm|cm|kg|mg|ml|litros|dólares|millones|habitantes|pacientes|personas|años|días|horas)");
    public static final Pattern P_CITA_PARENT = Pattern.compile("\\(\\d{1,2}(?:[,;]\\s*\\d{1,2})*\\)");
    public static final Pattern P_CITA_TEXTUAL = Pattern.compile("\"[^\"]{15,}\"|«[^»]{15,}»|“[^”]{15,}”");
    public static final Pattern P_PREGUNTA = Pattern.compile("¿");
        public static final Set<String> PRODUCTOR_REPROCESADO = Set.of(
            "qpdf", "pdfsam", "ghostscript", "pikepdf", "pdfmerge", "pdftk");

    private static final Map<String, Pattern> VOZ_PATTERNS = new HashMap<>();
    static {
        for (String m : VOZ_MARCADORES) VOZ_PATTERNS.put(m, Pattern.compile("\\b" + Pattern.quote(m) + "\\b"));
    }

    public static int contar(Pattern p, String texto) {
        Matcher m = p.matcher(texto);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    public static int contarVoz(String textoLower, String marcador) {
        Matcher m = VOZ_PATTERNS.get(marcador).matcher(textoLower);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    public static int contarSub(String texto, String sub) {
        int n = 0, idx = texto.indexOf(sub);
        while (idx >= 0) { n++; idx = texto.indexOf(sub, idx + sub.length()); }
        return n;
    }
}