package com.bibliography.analyzer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

/**
 * Límite de tasa por IP sobre los endpoints de análisis (POST),
 * para evitar abuso de créditos del motor externo.
 * app.rate.limit <= 0 desactiva el límite.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long VENTANA_MS = 60_000;

    @Value("${app.rate.limit:10}")
    private int maxPorMinuto;

    private final ConcurrentMap<String, Deque<Long>> visitas = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        boolean analisis = request.getMethod().equals("POST")
                && (request.getRequestURI().equals("/api/analizar-url")
                 || request.getRequestURI().equals("/api/analizar-documento"));

        if (!analisis || maxPorMinuto <= 0) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        long ahora = System.currentTimeMillis();
        Deque<Long> marca = visitas.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        synchronized (marca) {
            while (!marca.isEmpty() && ahora - marca.peekFirst() > VENTANA_MS) {
                marca.pollFirst();
            }
            if (marca.size() >= maxPorMinuto) {
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                    "{\"exito\":false,\"datos\":null,\"mensaje\":\"Demasiadas solicitudes en el último minuto. Intente nuevamente en unos segundos.\"}");
                return;
            }
            marca.addLast(ahora);
        }
        chain.doFilter(request, response);
    }
}