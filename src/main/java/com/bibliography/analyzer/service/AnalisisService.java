package com.bibliography.analyzer.service;

import com.bibliography.analyzer.analisis.*;
import com.bibliography.analyzer.dto.DatosAnalisis;
import org.slf4j.Logger;                                                
import org.slf4j.LoggerFactory;                                      
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;                               
import java.util.function.Function;

@Service
@ConditionalOnProperty(name = "app.modulo.pdf.enabled", havingValue = "true", matchIfMissing = true)
public class AnalisisService {

    private static final Logger log = LoggerFactory.getLogger(AnalisisService.class);
    /** Máximo 2 análisis PDF simultáneos: el módulo es CPU-intensivo. */
    private static final Semaphore SLOTS_PDF = new Semaphore(2);       

    private static final List<Function<TextoProcesado, Metrica>> PILARES_VOTANTES = List.of(
            Pilares::anclaje, Pilares::sintaxis, Pilares::burstiness, Pilares::voz, Pilares::intensificadores);

    private static final List<Function<TextoProcesado, Metrica>> TESTIGOS = List.of(
            Pilares::bigramas, Pilares::conectores, Pilares::parrafos);

    public DatosAnalisis analizarDocumento(MultipartFile archivo) throws IOException {
        // ===== Control de concurrencia: si no hay slot, rechazamos con 503 =====
        if (!SLOTS_PDF.tryAcquire()) {                                 
            throw new ServiceSaturedException("de análisis PDF");      
        }                                                             
        try {                                                         
            String id = UUID.randomUUID().toString().substring(0, 8);
            long t0 = System.currentTimeMillis();

            // CHECKPOINT: tiempo de extracción de texto + limpieza
            long tExtraccion = System.currentTimeMillis();           
            ExtractorPdf.Extraccion ex = ExtractorPdf.extraer(archivo.getBytes());
            String limpio = ExtractorPdf.limpiar(ex.textoCrudo());
            TextoProcesado t = new TextoProcesado(limpio, ExtractorPdf.parrafos(ex.textoCrudo()));
            log.info("[pdf:{}] extracción y limpieza en {} ms ({} palabras)",
                    id, System.currentTimeMillis() - tExtraccion, t.totalPalabras()); 

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

            // CHECKPOINT: tiempo de cálculo de pilares + testigos
            long tPilares = System.currentTimeMillis();                
            List<Metrica> votantes = PILARES_VOTANTES.parallelStream().map(f -> f.apply(t)).toList();
            List<Metrica> testigos = TESTIGOS.parallelStream().map(f -> f.apply(t)).toList();
            log.info("[pdf:{}] pilares y testigos calculados en {} ms",
                    id, System.currentTimeMillis() - tPilares);       

            double sp = 0, sw = 0;
            for (Metrica m : votantes) if (m.confiable()) { sp += m.peso() * m.score(); sw += m.peso(); }
            double scoreTexto = sw > 0 ? sp / sw : 50;

            HuellaDigital.Resultado hd = HuellaDigital.evaluar(ex.metadata(), t.totalPalabras());
            int indice = (int) Math.round(Math.max(0, Math.min(100, scoreTexto + hd.ajuste())));

            long ms = System.currentTimeMillis() - t0;
            StringBuilder sb = new StringBuilder("[" + id + "] " + Sanitizador.html(archivo.getOriginalFilename())
                    + " " + (archivo.getSize() / 1024) + "KB " + ms + "ms === CALIBRACIÓN === ");
            for (Metrica m : votantes) sb.append(m.nombre()).append('=').append(Math.round(m.score()))
                    .append(m.confiable() ? "" : "(nc)").append(m.puerta() ? "(puerta)" : "").append(' ');
            for (Metrica m : testigos) sb.append("[T]").append(m.nombre()).append('=').append(Math.round(m.score())).append(' ');
            sb.append("| texto=").append(Math.round(scoreTexto)).append(" meta=").append(hd.ajuste()).append(" | FINAL=").append(indice);
            log.info(sb.toString());                                   // <-- CAMBIADO de System.out.println a log.info

            return ReporteForense.construir(Sanitizador.html(archivo.getOriginalFilename()), indice, votantes, testigos, hd);
        } finally {                                                   
            SLOTS_PDF.release();                                      
        }                                                              
    }
}