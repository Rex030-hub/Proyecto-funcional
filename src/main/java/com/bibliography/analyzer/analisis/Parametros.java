package com.bibliography.analyzer.analisis;

public final class Parametros {
    private Parametros() {}

    // ===== Pesos Esquema R (solo pilares con señal; testigos = 0) =====
    public static final double W_ANCL = 0.35, W_SINT = 0.25, W_BURST = 0.15, W_VOZ = 0.10, W_BUZZ = 0.10;

    // ===== Umbrales (humano / IA) recalibrados con las 12 corridas =====
    public static final double ANCL_HUM = 1.2,  ANCL_IA = 0.3;
    public static final double SINT_HUM = 1.8,  SINT_IA = 0.6;
    public static final double BURST_HUM = 16.0, BURST_IA = 9.0;
    public static final double VOZ_HUM = 0.50,  VOZ_IA = 0.10;
    public static final double BUZZ_HUM = 0.05, BUZZ_IA = 0.30;
    public static final double BIG_HUM = 0.09,  BIG_IA = 0.14;   // testigo
    public static final double CON_HUM = 0.04,  CON_IA = 0.12;   // testigo
    public static final double PARR_HUM = 2.5,  PARR_IA = 1.0;   // testigo
    public static final double COB_VOZ_HUM = 0.20, COB_VOZ_IA = 0.10;
    public static final double COB_ANCL_HUM = 0.35, COB_ANCL_IA = 0.25;
    
    // ===== Bandas de veredicto =====-
    public static final int BANDA_HUMANO = 35, BANDA_IA = 65;

    // ===== Metadata =====
    public static final double META_TOPE = 10.0, META_AUTOR_MODELO = 10.0,
            META_NACIDO_COMPLETO = 8.0, META_EDITADO_EXTENSO = -8.0, META_EDITORIAL = -5.0;
    public static final int MIN_PALABRAS_NACIDO = 800, MAX_MINUTOS_NACIDO = 15, MIN_MINUTOS_EXTENSO = 1440;

    // valor ALTO = humano
    public static double interpInverso(double v, double hum, double ia) {
        if (v >= hum) return 0;
        if (v <= ia) return 100;
        return (hum - v) / (hum - ia) * 100;
    }
    // valor ALTO = IA
    public static double interpDirecto(double v, double hum, double ia) {
        if (v <= hum) return 0;
        if (v >= ia) return 100;
        return (v - hum) / (ia - hum) * 100;
    }
}