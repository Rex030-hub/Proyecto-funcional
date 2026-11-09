package com.bibliography.analyzer.analisis;

import com.bibliography.analyzer.dto.Estadistica;
import com.bibliography.analyzer.dto.Evidencia;
import com.bibliography.analyzer.service.ExtractorPdf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public final class HuellaDigital {
    public record Resultado(double ajuste, List<Estadistica> stats, List<Evidencia> evidencias) {}
    private HuellaDigital() {}

    public static Resultado evaluar(ExtractorPdf.MetadataExtraida meta, int palabras) {
        List<Estadistica> stats = new ArrayList<>();
        List<Evidencia> ev = new ArrayList<>();
        if (meta == null) {
            stats.add(new Estadistica("Huella digital", "Metadata no disponible (sin efecto en el puntaje)"));
            return new Resultado(0, stats, ev);
        }
        double ajuste = 0;
        String producer = meta.producer().toLowerCase();
        String autorTrim = meta.author().trim();
        String autorLow = autorTrim.toLowerCase();

        // Regla 1 (Parche 3): autoría de modelo solo con coincidencia EXACTA; parcial = nota neutral
        if (Diccionarios.NOMBRES_MODELO.contains(autorLow)) {
            ajuste += Parametros.META_AUTOR_MODELO;
            ev.add(new Evidencia("Huella digital", "Autoría declarada por un modelo",
                    "El campo de autor del PDF declara: " + Sanitizador.html(autorTrim) + ".",
                    "El propio documento atribuye su autoría a un modelo de lenguaje: confesión directa de generación automática."));
        } else {
            String parcial = Diccionarios.NOMBRES_MODELO.stream()
                    .filter(n -> Pattern.compile("\\b" + Pattern.quote(n) + "\\b").matcher(autorLow).find())
                    .findFirst().orElse(null);
            if (parcial != null) {
                ev.add(new Evidencia("Huella digital", "Campo autor ambiguo (sin efecto)",
                        "El campo autor (" + Sanitizador.html(autorTrim) + ") contiene el término '" + parcial + "'.",
                        "El término coincide con un nombre de modelo, pero también puede ser un nombre o apellido humano. Por prudencia, no se puntúa: es un dato declarativo no verificado."));
            } else if (!autorTrim.isEmpty()) {
                stats.add(new Estadistica("Huella digital: autor", Sanitizador.html(autorTrim)));
            } else {
                stats.add(new Estadistica("Huella digital: autor", "No declarado (sin efecto)"));
            }
        }

        // Regla 2 (Parche 6): clase de productor, incluida "reprocesado"
        String clase = "desconocido";
        if (!producer.isBlank()) {
            stats.add(new Estadistica("Huella digital: productor", Sanitizador.html(meta.producer())));
            if (Diccionarios.PRODUCTOR_REPROCESADO.stream().anyMatch(producer::contains)) {
                clase = "reprocesado";
                ev.add(new Evidencia("Huella digital", "Documento re-procesado (sin efecto)",
                        "El PDF fue re-escrito por: " + Sanitizador.html(meta.producer()) + ".",
                        "Las herramientas de manipulación de PDF borran la metadata original. La huella digital de este documento no es confiable y por eso no se puntúa."));
            } else if (Diccionarios.PRODUCTOR_EXPORT_DIRECTO.stream().anyMatch(producer::contains)) {
                clase = "directo";
            } else if (Diccionarios.PRODUCTOR_EDITORIAL.stream().anyMatch(producer::contains)) {
                clase = "editorial";
                ajuste += Parametros.META_EDITORIAL;
                ev.add(new Evidencia("Huella digital", "Cadena editorial profesional",
                        "El PDF fue producido por: " + Sanitizador.html(meta.producer()) + ".",
                        "Este motor corresponde a flujos editoriales o de composición académica, típico de publicaciones humanas."));
            } else if (Diccionarios.PRODUCTOR_CONVERSOR.stream().anyMatch(producer::contains)) {
                clase = "conversor";
            }
        }

        // Regla 3: vida del documento (solo exportación directa)
        Calendar cre = meta.creationDate();
        Calendar mod = meta.modificationDate();
        if (clase.equals("directo") && cre != null && mod != null) {
            long min = Math.abs(mod.getTimeInMillis() - cre.getTimeInMillis()) / 60000;
            String vida = min < 60 ? min + " min" : (min / 60) + " h " + (min % 60) + " min";
            stats.add(new Estadistica("Huella digital: vida del documento", vida));
            if (palabras > Parametros.MIN_PALABRAS_NACIDO && min < Parametros.MAX_MINUTOS_NACIDO) {
                ajuste += Parametros.META_NACIDO_COMPLETO;
                ev.add(new Evidencia("Huella digital", "Documento 'nacido completo'",
                        "Un texto de " + palabras + " palabras creado y modificado en solo " + vida + ".",
                        "La escritura humana deja historial de edición (horas o días). Un documento extenso sin historia sugiere generación y exportación instantánea."));
            } else if (min > Parametros.MIN_MINUTOS_EXTENSO) {
                ajuste += Parametros.META_EDITADO_EXTENSO;
                ev.add(new Evidencia("Huella digital", "Historial de edición extendido",
                        "El documento fue editado a lo largo de " + vida + ".",
                        "Un ciclo de edición de varios días es característico del trabajo humano de redacción y revisión."));
            }
        } else {
            stats.add(new Estadistica("Huella digital: fechas",
                    clase.equals("desconocido") ? "No declaradas (sin efecto)" : "Ignoradas (pipeline " + clase + ")"));
        }

        double acotado = Math.max(-Parametros.META_TOPE, Math.min(Parametros.META_TOPE, ajuste));
        return new Resultado(acotado, stats, ev);
    }
}