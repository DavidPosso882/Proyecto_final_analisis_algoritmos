package com.analisis.algoritmos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un activo financiero (accion o ETF).
 * 
 * Un activo es un instrumento financiero individual identificado por su ticker
 * y asociado a un mercado especifico (BVC, NYSE, NASDAQ, etc.).
 * 
 * @author David
 * @since 1.0.0
 */
@Entity
@Table(name = "assets")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    /**
     * Identificador unico del activo (autogenerado).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ticker del activo (ejemplo: ECOPETROL, VOO, AAPL).
     * Debe ser unico.
     */
    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    /**
     * Nombre completo del activo (ejemplo: Ecopetrol S.A.).
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Tipo de activo: STOCK (accion) o ETF.
     */
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private AssetType type;

    /**
     * Mercado donde cotiza (ejemplo: BVC, NYSE, NASDAQ).
     */
    @Column(nullable = false, length = 20)
    private String market;

    /**
     * Moneda de cotizacion (ejemplo: COP, USD).
     */
    @Column(length = 5)
    private String currency = "USD";

    /**
     * Sector industrial (opcional, para stocks).
     */
    @Column(length = 100)
    private String sector;

    /**
     * Fecha de creacion del registro.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha de ultima actualizacion.
     */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum para el tipo de activo.
     */
    public enum AssetType {
        STOCK,  // Accion
        ETF     // Exchange Traded Fund
    }
}
