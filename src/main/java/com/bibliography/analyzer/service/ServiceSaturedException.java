package com.bibliography.analyzer.service;

public class ServiceSaturedException extends RuntimeException {
    public ServiceSaturedException(String modulo) {
        super("El módulo " + modulo + " está saturado: hay demasiados análisis en curso. Intente nuevamente en unos segundos.");
    }
}