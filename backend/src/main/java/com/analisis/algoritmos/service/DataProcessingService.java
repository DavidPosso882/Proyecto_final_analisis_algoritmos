package com.analisis.algoritmos.service;

import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.PriceDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para procesamiento y limpieza de datos financieros.
 * 
 * Este servicio proporciona metodos para:
 * 1. Calcular retornos logaritmicos
 * 2. Calcular medias moviles simples (SMA)
 * 3. Detectar valores faltantes y anomalias
 * 4. Interpolar datos faltantes
 * 5. Validar consistencia de datos
 * 
 * NOTA: Todos los calculos se realizan manualmente sin usar librerias
 * de alto nivel como pandas (prohibido por requerimientos).
 * 
 * @author David
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataProcessingService {

    private final PriceDataRepository priceDataRepository;
    
    // Precision para calculos matematicos
    private static final MathContext MC = new MathContext(10);

    /**
     * Detecta huecos temporales (fechas faltantes) en una serie de datos.
     * 
     * Algoritmo: O(n) - Recorrido lineal de la serie
     * 
     * @param priceData Lista ordenada de datos de precios
     * @return Lista de fechas faltantes
     */
    public List<LocalDate> detectMissingDates(List<PriceData> priceData) {
        log.debug("Detectando fechas faltantes...");
        
        if (priceData == null || priceData.size() < 2) {
            return new ArrayList<>();
        }

        List<LocalDate> missingDates = new ArrayList<>();
        
        for (int i = 1; i < priceData.size(); i++) {
            LocalDate prevDate = priceData.get(i - 1).getDate();
            LocalDate currDate = priceData.get(i).getDate();
            
            // Calcular dias entre fechas
            long daysBetween = ChronoUnit.DAYS.between(prevDate, currDate);
            
            // Si hay mas de 1 dia de diferencia, hay fechas faltantes
            if (daysBetween > 1) {
                for (int j = 1; j < daysBetween; j++) {
                    missingDates.add(prevDate.plusDays(j));
                }
            }
        }
        
        log.debug("Fechas faltantes detectadas: {}", missingDates.size());
        return missingDates;
    }

    /**
     * Detecta outliers en una serie de precios usando el metodo IQR.
     * 
     * Algoritmo: O(n log n) - Por ordenamiento
     * 
     * Un outlier es un valor que cae fuera de [Q1 - 1.5*IQR, Q3 + 1.5*IQR]
     * 
     * @param priceData Lista de datos de precios
     * @return Lista de indices donde se detectaron outliers
     */
    public List<Integer> detectOutliers(List<PriceData> priceData) {
        log.debug("Detectando outliers...");
        
        if (priceData == null || priceData.size() < 4) {
            return new ArrayList<>();
        }

        // Extraer precios de cierre
        List<BigDecimal> closes = new ArrayList<>();
        for (PriceData data : priceData) {
            closes.add(data.getClosePrice());
        }

        // Ordenar para calcular percentiles
        List<BigDecimal> sorted = new ArrayList<>(closes);
        sorted.sort(BigDecimal::compareTo);

        int n = sorted.size();
        
        // Calcular Q1 (percentil 25) y Q3 (percentil 75)
        BigDecimal q1 = calculatePercentile(sorted, 0.25);
        BigDecimal q3 = calculatePercentile(sorted, 0.75);
        
        // Calcular IQR
        BigDecimal iqr = q3.subtract(q1);
        
        // Limites para outliers
        BigDecimal lowerBound = q1.subtract(iqr.multiply(BigDecimal.valueOf(1.5)));
        BigDecimal upperBound = q3.add(iqr.multiply(BigDecimal.valueOf(1.5)));
        
        // Detectar outliers
        List<Integer> outlierIndices = new ArrayList<>();
        for (int i = 0; i < closes.size(); i++) {
            BigDecimal value = closes.get(i);
            if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
                outlierIndices.add(i);
            }
        }
        
        log.debug("Outliers detectados: {}", outlierIndices.size());
        return outlierIndices;
    }

    /**
     * Calcula el percentil de una lista ordenada.
     * 
     * @param sorted Lista ordenada de valores
     * @param percentile Percentil a calcular (0.0 a 1.0)
     * @return Valor del percentil
     */
    private BigDecimal calculatePercentile(List<BigDecimal> sorted, double percentile) {
        int n = sorted.size();
        double index = percentile * (n - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex);
        }
        
        double weight = index - lowerIndex;
        BigDecimal lower = sorted.get(lowerIndex);
        BigDecimal upper = sorted.get(upperIndex);
        
        return lower.add(upper.subtract(lower).multiply(BigDecimal.valueOf(weight)));
    }

    /**
     * Calcula los retornos logaritmicos de una serie de precios.
     * 
     * Formula: r_t = ln(P_t / P_{t-1})
     * 
     * Algoritmo: O(n) - Recorrido lineal
     * 
     * @param priceData Lista ordenada de datos de precios
     * @return Lista de retornos logaritmicos (null para el primer elemento)
     */
    public List<BigDecimal> calculateLogReturns(List<PriceData> priceData) {
        log.debug("Calculando retornos logaritmicos...");
        
        if (priceData == null || priceData.size() < 2) {
            return new ArrayList<>();
        }

        List<BigDecimal> logReturns = new ArrayList<>();
        logReturns.add(null); // Primer elemento no tiene retorno
        
        for (int i = 1; i < priceData.size(); i++) {
            BigDecimal currentPrice = priceData.get(i).getClosePrice();
            BigDecimal previousPrice = priceData.get(i - 1).getClosePrice();
            
            if (previousPrice.compareTo(BigDecimal.ZERO) > 0) {
                // r_t = ln(P_t / P_{t-1})
                double ratio = currentPrice.doubleValue() / previousPrice.doubleValue();
                double logReturn = Math.log(ratio);
                logReturns.add(BigDecimal.valueOf(logReturn));
            } else {
                logReturns.add(null);
            }
        }
        
        return logReturns;
    }

    /**
     * Calcula la Media Movil Simple (SMA) para un periodo dado.
     * 
     * Formula: SMA_k = (P_t + P_{t-1} + ... + P_{t-k+1}) / k
     * 
     * Algoritmo: O(n) con ventana deslizante optimizada
     * 
     * @param priceData Lista de datos de precios
     * @param period Periodo de la media movil (ej: 20, 50)
     * @return Lista de SMA (null para los primeros period-1 elementos)
     */
    public List<BigDecimal> calculateSMA(List<PriceData> priceData, int period) {
        log.debug("Calculando SMA-{}...", period);
        
        if (priceData == null || priceData.size() < period) {
            return new ArrayList<>();
        }

        List<BigDecimal> sma = new ArrayList<>();
        
        // Primeros (period-1) elementos no tienen SMA
        for (int i = 0; i < period - 1; i++) {
            sma.add(null);
        }
        
        // Calcular primera ventana
        BigDecimal windowSum = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            windowSum = windowSum.add(priceData.get(i).getClosePrice());
        }
        sma.add(windowSum.divide(BigDecimal.valueOf(period), MC));
        
        // Ventana deslizante optimizada: O(n)
        for (int i = period; i < priceData.size(); i++) {
            // Restar elemento que sale, sumar elemento que entra
            BigDecimal outgoing = priceData.get(i - period).getClosePrice();
            BigDecimal incoming = priceData.get(i).getClosePrice();
            
            windowSum = windowSum.subtract(outgoing).add(incoming);
            sma.add(windowSum.divide(BigDecimal.valueOf(period), MC));
        }
        
        return sma;
    }

    /**
     * Interpola linealmente un valor faltante entre dos puntos conocidos.
     * 
     * Formula: P_interpolado = P1 + (P2 - P1) * (t - t1) / (t2 - t1)
     * 
     * @param price1 Precio en t1
     * @param price2 Precio en t2
     * @param daysBetween Dias entre t1 y t2
     * @param dayOffset Offset desde t1 (1 a daysBetween-1)
     * @return Precio interpolado
     */
    public BigDecimal linearInterpolate(
            BigDecimal price1, 
            BigDecimal price2, 
            long daysBetween, 
            long dayOffset) {
        
        if (daysBetween <= 1 || dayOffset <= 0 || dayOffset >= daysBetween) {
            throw new IllegalArgumentException("Parametros de interpolacion invalidos");
        }
        
        double fraction = (double) dayOffset / daysBetween;
        BigDecimal difference = price2.subtract(price1);
        BigDecimal interpolation = difference.multiply(BigDecimal.valueOf(fraction));
        
        return price1.add(interpolation);
    }

    /**
     * Valida la consistencia de una serie de datos.
     * Verifica:
     * - Orden cronologico
     * - Consistencia de precios (Low <= Open/Close <= High)
     * - Volumen positivo
     * 
     * @param priceData Lista de datos a validar
     * @return Lista de errores encontrados (vacia si todo es valido)
     */
    public List<String> validateDataConsistency(List<PriceData> priceData) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < priceData.size(); i++) {
            PriceData data = priceData.get(i);
            
            // Validar orden cronologico
            if (i > 0) {
                LocalDate prevDate = priceData.get(i - 1).getDate();
                if (!data.getDate().isAfter(prevDate)) {
                    errors.add(String.format(
                        "Error en indice %d: Fechas desordenadas (%s <= %s)",
                        i, data.getDate(), prevDate
                    ));
                }
            }
            
            // Validar consistencia de precios
            if (!data.isValidPriceRange()) {
                errors.add(String.format(
                    "Error en %s (%s): Rango de precios invalido",
                    data.getAsset().getTicker(), data.getDate()
                ));
            }
            
            // Validar volumen positivo
            if (data.getVolume() < 0) {
                errors.add(String.format(
                    "Error en %s (%s): Volumen negativo",
                    data.getAsset().getTicker(), data.getDate()
                ));
            }
        }
        
        return errors;
    }
}
