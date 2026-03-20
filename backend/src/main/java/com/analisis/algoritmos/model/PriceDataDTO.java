package com.analisis.algoritmos.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para transferir datos de precios sin problemas de serialización de Hibernate.
 */
public record PriceDataDTO(
    Long id,
    String ticker,
    LocalDate date,
    BigDecimal openPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal closePrice,
    Long volume,
    BigDecimal adjustedClose,
    Boolean interpolated,
    Boolean nonTradingDay
) {
    public static PriceDataDTO fromEntity(PriceData entity) {
        return new PriceDataDTO(
            entity.getId(),
            entity.getAsset() != null ? entity.getAsset().getTicker() : null,
            entity.getDate(),
            entity.getOpenPrice(),
            entity.getHighPrice(),
            entity.getLowPrice(),
            entity.getClosePrice(),
            entity.getVolume(),
            entity.getAdjustedClose(),
            entity.getInterpolated(),
            entity.getNonTradingDay()
        );
    }
}
