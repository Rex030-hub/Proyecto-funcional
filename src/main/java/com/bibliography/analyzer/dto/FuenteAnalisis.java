package com.bibliography.analyzer.dto;
import java.util.List;
public record FuenteAnalisis(FuenteDatos fuente, boolean desdeCache,
                             List<FuenteDatos> relacionadas, long duracionMs) {}