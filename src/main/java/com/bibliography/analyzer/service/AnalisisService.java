package com.bibliography.analyzer.service;

import com.bibliography.analyzer.analisis.*;
import com.bibliography.analyzer.dto.DatosAnalisis;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class AnalisisService {

    private static final List<Function<TextoProcesado, Metrica>> PILARES_VOTANTES = List.of(
            Pilares::anclaje, Pilares::sintaxis, Pilares::burstiness, Pilares::voz, Pilares::intensificadores);

    private static final List<Function<TextoProcesado, Metrica>> TESTIGOS = List.of(
            Pilares::bigramas, Pilares::conectores, Pilares::parrafos);

    public DatosAnalisis analizarDocumento(MultipartFile archivo) throws IOException {
        String id = UUID.randomUUID().toString().substring(0, 8);
        long t0 = System.currentTimeMillis();

        ExtractorPdf.Extraccion ex = ExtractorPdf.extraer(archivo.getBytes());
        String limpio = ExtractorPdf.limpiar(ex.textoCrudo());
        TextoProcesado t = new TextoProcesado(limpio, ExtractorPdf.parrafos(ex.textoCrudo()));

        // Parche 1: escaneados / sin texto suficiente
        if (t.totalPalabras() < 100) {
            throw new IllegalArgumentException("El PDF no contiene una capa de texto legible (posible documento escaneado) o el texto es insuficiente para el análisis.");
        }
        // Parche 2: puerta de idioma
        if (!Idioma.esEspanol(limpio)) {
            if (Idioma.extraccionFragmentada(limpio)) {
                throw new IllegalArgumentException("Idioma detectado no soportado: el sistema está calibrado únicamente para español. Veredicto no disponible para este documento.");
            }
            throw new IllegalArgumentException("El texto del PDF se extrajo fragmentado (fuentes con codificación no estándar): veredicto no disponible por calidad de extracción insuficiente.");
        }

        List<Metrica> votantes = PILARES_VOTANTES.parallelStream().map(f -> f.apply(t)).toList();
        List<Metrica> testigos = TESTIGOS.parallelStream().map(f -> f.apply(t)).toList();

        double sp = 0, sw = 0;
        for (Metrica m : votantes) if (m.confiable()) { sp += m.peso() * m.score(); sw += m.peso(); }
        double scoreTexto = sw > 0 ? sp / sw : 50;

        HuellaDigital.Resultado hd = HuellaDigital.evaluar(ex.metadata(), t.totalPalabras());
        int indice = (int) Math.round(Math.max(0, Math.min(100, scoreTexto + hd.ajuste())));

        long ms = System.currentTimeMillis() - t0;
        StringBuilder log = new StringBuilder("[" + id + "] " + Sanitizador.html(archivo.getOriginalFilename())
                + " " + (archivo.getSize() / 1024) + "KB " + ms + "ms === CALIBRACIÓN === ");
        for (Metrica m : votantes) log.append(m.nombre()).append('=').append(Math.round(m.score())).append(m.confiable() ? "" : "(nc)").append(m.puerta() ? "(puerta)" : "").append(' ');
        for (Metrica m : testigos) log.append("[T]").append(m.nombre()).append('=').append(Math.round(m.score())).append(' ');
        log.append("| texto=").append(Math.round(scoreTexto)).append(" meta=").append(hd.ajuste()).append(" | FINAL=").append(indice);
        System.out.println(log);

        return ReporteForense.construir(Sanitizador.html(archivo.getOriginalFilename()), indice, votantes, testigos, hd);
    }
}