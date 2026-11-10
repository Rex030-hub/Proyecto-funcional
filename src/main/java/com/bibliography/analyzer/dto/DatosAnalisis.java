package com.bibliography.analyzer.dto;

import java.util.List;

public class DatosAnalisis {
    private String nombre;
    private String porcentaje;
    private String interpretacion;
    private List<Estadistica> estadisticas;
    private List<String> indicadores;
    private List<Evidencia> evidencias;

    public DatosAnalisis() {}

    public DatosAnalisis(String nombre, String porcentaje, String interpretacion, 
                         List<Estadistica> estadisticas, List<String> indicadores, 
                         List<Evidencia> evidencias) {
        this.nombre = nombre;
        this.porcentaje = porcentaje;
        this.interpretacion = interpretacion;
        this.estadisticas = estadisticas;
        this.indicadores = indicadores;
        this.evidencias = evidencias;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPorcentaje() { return porcentaje; }
    public void setPorcentaje(String porcentaje) { this.porcentaje = porcentaje; }
    public String getInterpretacion() { return interpretacion; }
    public void setInterpretacion(String interpretacion) { this.interpretacion = interpretacion; }
    public List<Estadistica> getEstadisticas() { return estadisticas; }
    public void setEstadisticas(List<Estadistica> estadisticas) { this.estadisticas = estadisticas; }
    public List<String> getIndicadores() { return indicadores; }
    public void setIndicadores(List<String> indicadores) { this.indicadores = indicadores; }
    public List<Evidencia> getEvidencias() { return evidencias; }
    public void setEvidencias(List<Evidencia> evidencias) { this.evidencias = evidencias; }
}