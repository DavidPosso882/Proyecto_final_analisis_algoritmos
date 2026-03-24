package com.analisis.algoritmos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa los datos de precios de un activo para una fecha especifica.
 */
@Entity
@Table(name = "price_data", 
       indexes = {
           @Index(name = "idx_price_asset_date", columnList = "asset_id, date"),
           @Index(name = "idx_price_date", columnList = "date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Asset getAsset() {
        return asset;
    }

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal openPrice;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal highPrice;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal lowPrice;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal closePrice;

    @Column(nullable = false)
    private Long volume;

    @Column(precision = 15, scale = 4)
    private BigDecimal adjustedClose;

    @Column(nullable = false)
    private Boolean interpolated = false;

    @Column(nullable = false)
    private Boolean nonTradingDay = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isValidPriceRange() {
        if (openPrice == null || highPrice == null || lowPrice == null || closePrice == null) return false;
        return lowPrice.compareTo(openPrice) <= 0 &&
               openPrice.compareTo(highPrice) <= 0 &&
               lowPrice.compareTo(closePrice) <= 0 &&
               closePrice.compareTo(highPrice) <= 0;
    }

    public BigDecimal getDailyRange() {
        if (highPrice == null || lowPrice == null) return BigDecimal.ZERO;
        return highPrice.subtract(lowPrice);
    }

    public BigDecimal getDailyChangePercent() {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0 || closePrice == null) {
            return BigDecimal.ZERO;
        }
        return closePrice.subtract(openPrice)
                .divide(openPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
