package com.bibliography.analyzer.dto;

public class AnalisisRespuesta {
    private boolean exito;
    private DatosAnalisis datos;
    private String mensaje;

    public AnalisisRespuesta() {}

    public AnalisisRespuesta(boolean exito, DatosAnalisis datos, String mensaje) {
        this.exito = exito;
        this.datos = datos;
        this.mensaje = mensaje;
    }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public DatosAnalisis getDatos() { return datos; }
    public void setDatos(DatosAnalisis datos) { this.datos = datos; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}