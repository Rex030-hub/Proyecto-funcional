package com.bibliography.analyzer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Exige la llave de administración (header X-Admin-Key) para todo el CRUD
 * de fuentes (/api/fuentes/**). El frontend no consume estos endpoints:
 * solo Postman o tareas de administración.
 * Si app.admin.key está vacía, el filtro no bloquea (desarrollo local).
 */
@Component
public class WriteAuthFilter extends OncePerRequestFilter {

    @Value("${app.admin.key:}")
    private String adminKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        boolean protegido = request.getRequestURI().startsWith("/api/fuentes");
        boolean preflight = request.getMethod().equals("OPTIONS");

        if (protegido && !preflight && !adminKey.isBlank()
                && !adminKey.equals(request.getHeader("X-Admin-Key"))) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                "{\"exito\":false,\"datos\":null,\"mensaje\":\"Operación no autorizada: requiere llave de administración.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}