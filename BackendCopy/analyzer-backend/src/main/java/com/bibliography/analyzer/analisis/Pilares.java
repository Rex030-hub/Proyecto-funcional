package com.bibliography.analyzer.analisis;

import java.util.ArrayList;
import java.util.List;

public final class Pilares {
    private Pilares() {}
    private static final String SIGNOS_COMPLEJOS = ";:()«»“”\"…—";

    // ================= PILARES VOTANTES =================
    public static Metrica anclaje(TextoProcesado t) {
        if (t.totalPalabras() < 200) return new Metrica("Anclaje empírico", 50, false, 0, Parametros.W_ANCL);
        double dens = contarAnclas(t.texto()) * 100.0 / t.totalPalabras();
        double scoreDens = Parametros.interpInverso(dens, Parametros.ANCL_HUM, Parametros.ANCL_IA);
        if (t.parrafos().size() >= 4 && scoreDens <= 35) {
            int conAncla = 0;
            for (String p : t.parrafos()) if (contarAnclas(p) > 0) conAncla++;
            double cob = (double) conAncla / t.parrafos().size();
            double scoreCob = Parametros.interpInverso(cob, Parametros.COB_ANCL_HUM, Parametros.COB_ANCL_IA);
            if (scoreCob >= 65) return new Metrica("Anclaje empírico", scoreCob, true, dens, Parametros.W_ANCL, true);
        }
        return new Metrica("Anclaje empírico", scoreDens, true, dens, Parametros.W_ANCL);
    }

    public static Metrica sintaxis(TextoProcesado t) {
        if (t.totalPalabras() < 200) return new Metrica("Complejidad sintáctica", 50, false, 0, Parametros.W_SINT);
        int c = 0;
        for (char ch : t.texto().toCharArray()) if (SIGNOS_COMPLEJOS.indexOf(ch) >= 0) c++;
        double d = c * 100.0 / t.totalPalabras();
        return new Metrica("Complejidad sintáctica", Parametros.interpInverso(d, Parametros.SINT_HUM, Parametros.SINT_IA), true, d, Parametros.W_SINT);
    }

    public static Metrica burstiness(TextoProcesado t) {
        if (t.oraciones().size() < 8) return new Metrica("Burstiness", 50, false, 0, Parametros.W_BURST);
        List<Integer> longs = new ArrayList<>();
        for (String o : t.oraciones()) longs.add(o.split("\\s+").length);
        double de = desviacion(longs);
        return new Metrica("Burstiness", Parametros.interpInverso(de, Parametros.BURST_HUM, Parametros.BURST_IA), true, de, Parametros.W_BURST);
    }

    public static Metrica voz(TextoProcesado t) {
        if (t.totalPalabras() < 200) return new Metrica("Voz autoral", 50, false, 0, Parametros.W_VOZ);
        double dens = contarVoz(t.texto()) * 100.0 / t.totalPalabras();
        double scoreDens = Parametros.interpInverso(dens, Parametros.VOZ_HUM, Parametros.VOZ_IA);
        if (t.parrafos().size() >= 4 && scoreDens <= 35) {
            int conVoz = 0;
            for (String p : t.parrafos()) if (contarVoz(p) > 0) conVoz++;
            double cob = (double) conVoz / t.parrafos().size();
            double scoreCob = Parametros.interpInverso(cob, Parametros.COB_VOZ_HUM, Parametros.COB_VOZ_IA);
            if (scoreCob >= 65) return new Metrica("Voz autoral", scoreCob, true, dens, Parametros.W_VOZ, true);
        }
        return new Metrica("Voz autoral", scoreDens, true, dens, Parametros.W_VOZ);
    }

    public static Metrica intensificadores(TextoProcesado t) {
        if (t.totalPalabras() < 200) return new Metrica("Intensificadores vacíos", 50, false, 0, Parametros.W_BUZZ);
        int c = 0;
        for (String s : Diccionarios.INTENSIFICADORES) c += Diccionarios.contarSub(t.lower(), s);
        double d = c * 100.0 / t.totalPalabras();
        return new Metrica("Intensificadores vacíos", Parametros.interpDirecto(d, Parametros.BUZZ_HUM, Parametros.BUZZ_IA), true, d, Parametros.W_BUZZ);
    }

    // ================= TESTIGOS (peso 0) =================
    public static Metrica bigramas(TextoProcesado t) {
        if (t.totalPalabras() < 200 || t.bigramas().isEmpty()) return new Metrica("Repetición de bigramas", 50, false, 0, 0);
        long rep = t.bigramas().values().stream().filter(v -> v > 1).count();
        double r = (double) rep / t.bigramas().size();
        return new Metrica("Repetición de bigramas", Parametros.interpDirecto(r, Parametros.BIG_HUM, Parametros.BIG_IA), true, r, 0);
    }

    public static Metrica conectores(TextoProcesado t) {
        if (t.oraciones().size() < 10) return new Metrica("Conectores vacíos", 50, false, 0, 0);
        long c = t.oraciones().stream()
                .filter(o -> { String l = o.toLowerCase(); return Diccionarios.CONECTORES_VACIOS.stream().anyMatch(l::contains); })
                .count();
        double r = (double) c / t.oraciones().size();
        return new Metrica("Conectores vacíos", Parametros.interpDirecto(r, Parametros.CON_HUM, Parametros.CON_IA), true, r, 0);
    }

    public static Metrica parrafos(TextoProcesado t) {
        List<Integer> n = new ArrayList<>();
        for (String p : t.parrafos()) {
            int or = 0;
            for (String o : p.split("(?<=[.!?])\\s+")) {
                int w = o.trim().split("\\s+").length;
                if (w > 3 && w < 120) or++;
            }
            if (or > 0) n.add(or);
        }
        if (n.size() < 4) return new Metrica("Uniformidad de párrafos", 50, false, 0, 0);
        double de = desviacion(n);
        return new Metrica("Uniformidad de párrafos", Parametros.interpInverso(de, Parametros.PARR_HUM, Parametros.PARR_IA), true, de, 0);
    }

    // ================= HELPERS =================
    private static int contarAnclas(String s) {
        return Diccionarios.contar(Diccionarios.P_ANIOS, s)
                + Diccionarios.contar(Diccionarios.P_NUM_UNIDAD, s)
                + Diccionarios.contar(Diccionarios.P_CITA_PARENT, s)
                + Diccionarios.contar(Diccionarios.P_CITA_TEXTUAL, s);
    }

    private static int contarVoz(String s) {
        String low = s.toLowerCase();
        int c = 0;
        for (String m : Diccionarios.VOZ_MARCADORES) c += Diccionarios.contarVoz(low, m);
        c += Diccionarios.contar(Diccionarios.P_PREGUNTA, s);
        for (String a : Diccionarios.ANALOGIAS) c += Diccionarios.contarSub(low, a);
        return c;
    }

    private static double desviacion(List<Integer> v) {
        double m = v.stream().mapToInt(Integer::intValue).average().orElse(0);
        double var = v.stream().mapToDouble(x -> (x - m) * (x - m)).average().orElse(0);
        return Math.sqrt(var);
    }
}