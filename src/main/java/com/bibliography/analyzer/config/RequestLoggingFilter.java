package com.bibliography.analyzer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Genera un id de correlación por petición, lo pone en MDC (visible en todos
 * los logs de la cadena) y registra INICIO/FIN con status y duración total.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    public static final String REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID, requestId);
        response.setHeader("X-Request-Id", requestId);
        long t0 = System.currentTimeMillis();
        log.info("INICIO {} {} desde {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = System.currentTimeMillis() - t0;
            log.info("FIN {} {} status={} total={} ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), ms);
            MDC.clear();
        }
    }
}