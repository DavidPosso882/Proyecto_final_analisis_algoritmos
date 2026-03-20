package com.analisis.algoritmos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Proyecto Final - Analisis de Algoritmos
 * Universidad del Quindio - 2026-1
 * 
 * Sistema de analisis tecnico, estadistico y comparativo de activos financieros.
 * Implementa algoritmos de similitud manualmente (Euclidiana, Pearson, DTW, Coseno)
 * y analisis de patrones con sliding window.
 * 
 * Stack:
 * - Java 21
 * - Spring Boot 3.2
 * - MariaDB / H2
 * - Maven
 * 
 * @author David
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
