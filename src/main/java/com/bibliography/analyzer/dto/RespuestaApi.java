package com.bibliography.analyzer.dto;
public record RespuestaApi(boolean exito, Object datos, String mensaje) {
    public static RespuestaApi ok(Object datos) { return new RespuestaApi(true, datos, null); }
    public static RespuestaApi error(String mensaje) { return new RespuestaApi(false, null, mensaje); }
}