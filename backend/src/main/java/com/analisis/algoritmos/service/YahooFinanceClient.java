package com.analisis.algoritmos.service;

import com.analisis.algoritmos.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class YahooFinanceClient {

    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    private final RestTemplate restTemplate;
    
    public YahooFinanceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record YahooChartResponse(
        Map<String, PriceData> prices,
        List<String> errors
    ) {}

    public YahooChartResponse downloadHistoricalData(String rawTicker, LocalDate startDate, LocalDate endDate) {
        // Ajustar ticker para BVC (Bolsa de Valores de Colombia) en Yahoo Finance
        String ticker = adjustTickerForYahoo(rawTicker);
        log.info("Descargando datos de Yahoo Finance para: {} (Original: {}) ({})", ticker, rawTicker, startDate);
        
        Map<String, PriceData> prices = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        try {
            long period1 = startDate.toEpochDay() * 86400;
            long period2 = endDate.toEpochDay() * 86400;
            
            String url = String.format("%s%s?period1=%d&period2=%d&interval=1d", 
                BASE_URL, ticker, period1, period2);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Accept", "application/json");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                Map.class
            );
            
            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                if (body.containsKey("chart")) {
                    Map<String, Object> chart = (Map<String, Object>) body.get("chart");
                    List<Object> resultList = (List<Object>) chart.get("result");
                    
                    if (resultList != null && !resultList.isEmpty()) {
                        Map<String, Object> result = (Map<String, Object>) resultList.get(0);
                        Map<String, Object> indicators = (Map<String, Object>) result.get("indicators");
                        
                        if (indicators != null) {
                            List<Map<String, Object>> quote = (List<Map<String, Object>>) indicators.get("quote");
                            
                            if (quote != null && !quote.isEmpty()) {
                                Map<String, Object> quoteData = quote.get(0);
                                List<Map<String, Object>> adjcloseList = (List<Map<String, Object>>) indicators.get("adjclose");
                                
                                // Usar List<?> para evitar ClassCastException si Yahoo devuelve Integer en vez de Double
                                List<?> timestamps = (List<?>) result.get("timestamp");
                                List<?> opens = (List<?>) quoteData.get("open");
                                List<?> highs = (List<?>) quoteData.get("high");
                                List<?> lows = (List<?>) quoteData.get("low");
                                List<?> closes = (List<?>) quoteData.get("close");
                                List<?> volumes = (List<?>) quoteData.get("volume");
                                
                                List<?> adjcloses = null;
                                if (adjcloseList != null && !adjcloseList.isEmpty()) {
                                    adjcloses = (List<?>) adjcloseList.get(0).get("adjclose");
                                }
                                
                                if (timestamps != null) {
                                    for (int i = 0; i < timestamps.size(); i++) {
                                        try {
                                            Number tsRaw = getValueAt(timestamps, i);
                                            long ts = tsRaw != null ? tsRaw.longValue() : 0L;
                                            
                                            if (ts == 0) continue;
                                            
                                            LocalDate date = Instant.ofEpochSecond(ts)
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDate();
                                            
                                            PriceData priceData = new PriceData();
                                            priceData.setDate(date);
                                            priceData.setOpenPrice(toBigDecimal(getValueAt(opens, i)));
                                            priceData.setHighPrice(toBigDecimal(getValueAt(highs, i)));
                                            priceData.setLowPrice(toBigDecimal(getValueAt(lows, i)));
                                            priceData.setClosePrice(toBigDecimal(getValueAt(closes, i)));
                                            priceData.setVolume(toLong(getValueAt(volumes, i)));
                                            
                                            if (adjcloses != null && i < adjcloses.size() && getValueAt(adjcloses, i) != null) {
                                                priceData.setAdjustedClose(toBigDecimal(getValueAt(adjcloses, i)));
                                            }
                                            
                                            priceData.setInterpolated(false);
                                            priceData.setNonTradingDay(false);
                                            
                                            // Usar rawTicker para mantener coherencia en la base de datos interna
                                            prices.put(rawTicker + "_" + date, priceData);
                                            
                                        } catch (Exception e) {
                                            log.warn("Error parseando registro {} para {}: {}", i, rawTicker, e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (body.containsKey("error") && body.get("error") != null) {
                    Map<String, Object> error = (Map<String, Object>) body.get("error");
                    String errorDesc = (String) error.get("description");
                    errors.add(rawTicker + ": " + errorDesc);
                    log.error("Error de Yahoo Finance para {}: {}", rawTicker, errorDesc);
                }
            }
            
            log.info("Descargados {} registros para {}", prices.size(), rawTicker);
            
        } catch (Exception e) {
            errors.add(rawTicker + ": " + e.getMessage());
            log.error("Error descargando datos para {}: {}", rawTicker, e.getMessage());
        }
        
        return new YahooChartResponse(prices, errors);
    }

    /**
     * Ajusta el ticker para Yahoo Finance. Las acciones colombianas
     * suelen requerir el sufijo .CL o .BVC dependiendo de cómo coticen en Yahoo.
     */
    private String adjustTickerForYahoo(String ticker) {
        List<String> bvcTickers = List.of(
            "ECOPETROL", "BCOLOMBIA", "PFBCOLOM", "ISA", "GEB", 
            "CELSIA", "BOGOTA", "NUTRESA", "GRUPOARGOS", "CORFICOLCF"
        );
        
        if (bvcTickers.contains(ticker.toUpperCase())) {
            // La mayoría de activos líquidos de Colombia en Yahoo Finance usan .CL (Chile) 
            // a veces o no tienen info. En la vida real algunos son ADRs en NYSE.
            // Para el alcance de la prueba académica, intentamos .CL que es lo más común
            // para mercados Latinoamericanos cuando no hay ADR.
            return ticker.toUpperCase() + ".CL"; 
        }
        return ticker;
    }

    public boolean testConnection() {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "AAPL?interval=1d&range=1d",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error probando conexión con Yahoo Finance: {}", e.getMessage());
            return false;
        }
    }

    private Number getValueAt(List<?> list, int index) {
        if (list == null || index >= list.size()) {
            return 0;
        }
        Object value = list.get(index);
        if (value == null) {
            return 0;
        }
        return (Number) value;
    }

    private BigDecimal toBigDecimal(Number number) {
        if (number == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(number.doubleValue());
    }

    private Long toLong(Number number) {
        if (number == null) {
            return 0L;
        }
        return number.longValue();
    }
}
