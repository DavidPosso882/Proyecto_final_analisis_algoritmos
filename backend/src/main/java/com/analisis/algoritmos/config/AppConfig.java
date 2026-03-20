package com.analisis.algoritmos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de la aplicación.
 *
 * Incluye:
 * - RestTemplate para peticiones HTTP (ETL con Yahoo Finance)
 * - Configuración CORS centralizada para el frontend React
 *
 * @author David
 * @since 1.0.0
 */
@Configuration
public class AppConfig {

    /**
     * Bean de RestTemplate para peticiones HTTP al API de Yahoo Finance.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configuración CORS global.
     * Permite solicitudes desde el frontend React (localhost:3000/3001).
     * En producción, reemplazar con la URL real del frontend.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000", "http://localhost:3001")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
