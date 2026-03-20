package com.analisis.algoritmos.controller;

import com.analisis.algoritmos.algorithms.preprocessing.ReturnsCalculator;
import com.analisis.algoritmos.algorithms.preprocessing.SimpleMovingAverage;
import com.analisis.algoritmos.algorithms.similarity.PearsonCorrelation;
import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.AssetRepository;
import com.analisis.algoritmos.repository.PriceDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API REST para análisis avanzado de datos financieros.
 *
 * Endpoints para:
 * - Matriz de correlación real (Pearson) entre todos los activos
 * - Medias móviles simples (SMA) calculadas manualmente
 * - Retornos logarítmicos diarios calculados manualmente
 *
 * TODOS los cálculos se realizan manualmente usando los algoritmos
 * implementados en el paquete algorithms/, sin librerías de alto nivel.
 *
 * @author David
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AssetRepository assetRepository;
    private final PriceDataRepository priceDataRepository;
    private final PearsonCorrelation pearsonCorrelation;
    private final SimpleMovingAverage simpleMovingAverage;
    private final ReturnsCalculator returnsCalculator;

    /**
     * Obtiene la matriz de correlación real entre todos los activos.
     *
     * Calcula la correlación de Pearson entre los retornos logarítmicos
     * de cada par de activos usando el algoritmo implementado manualmente
     * en PearsonCorrelation.java.
     *
     * Complejidad: O(a² * n) donde a = número de activos, n = número de fechas.
     *
     * @return Matriz de correlación con tickers y valores reales
     */
    @GetMapping("/correlation-matrix")
    public ResponseEntity<CorrelationMatrixResponse> getCorrelationMatrix() {
        List<Asset> assets = assetRepository.findAllByOrderByTickerAsc();
        List<String> tickers = assets.stream()
                .map(Asset::getTicker)
                .collect(Collectors.toList());

        int size = assets.size();
        double[][] matrix = new double[size][size];

        // Precalcular retornos logarítmicos para cada activo
        Map<String, List<Double>> returnsMap = new HashMap<>();
        for (Asset asset : assets) {
            List<PriceData> prices = priceDataRepository.findByAssetOrderByDateAsc(asset);
            List<BigDecimal> closePrices = prices.stream()
                    .map(PriceData::getClosePrice)
                    .collect(Collectors.toList());

            List<Double> logReturns = returnsCalculator.calculateLogReturns(closePrices);
            // Filtrar nulls de los retornos
            List<Double> validReturns = logReturns.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            returnsMap.put(asset.getTicker(), validReturns);
        }

        // Calcular la matriz de correlación de Pearson par a par
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    matrix[i][j] = 1.0; // Correlación consigo mismo = 1
                } else if (i < j) {
                    List<Double> returnsA = returnsMap.get(tickers.get(i));
                    List<Double> returnsB = returnsMap.get(tickers.get(j));

                    // Alinear longitudes (tomar mínimo)
                    int minLen = Math.min(returnsA.size(), returnsB.size());
                    if (minLen > 1) {
                        List<Double> alignedA = returnsA.subList(returnsA.size() - minLen, returnsA.size());
                        List<Double> alignedB = returnsB.subList(returnsB.size() - minLen, returnsB.size());

                        Double correlation = pearsonCorrelation.calculateWithDouble(alignedA, alignedB);
                        matrix[i][j] = correlation != null ? correlation : 0.0;
                    } else {
                        matrix[i][j] = 0.0;
                    }
                } else {
                    matrix[i][j] = matrix[j][i]; // Matriz simétrica
                }
            }
        }

        CorrelationMatrixResponse response = new CorrelationMatrixResponse(
            tickers,
            matrix,
            "Matriz de correlación de Pearson calculada sobre retornos logarítmicos",
            "Correlación calculada manualmente con algoritmo propio (PearsonCorrelation.java)"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Calcula la media móvil simple (SMA) real para un activo.
     *
     * Usa el algoritmo implementado manualmente en SimpleMovingAverage.java
     * que emplea ventana deslizante O(n).
     *
     * @param ticker Símbolo del activo
     * @param periods Períodos para SMA (default: 20)
     * @return Valores SMA calculados manualmente
     */
    @GetMapping("/sma/{ticker}")
    public ResponseEntity<SmaResponse> calculateSMA(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "20") int periods) {

        Optional<Asset> assetOpt = assetRepository.findByTicker(ticker.toUpperCase());

        if (assetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Asset asset = assetOpt.get();
        List<PriceData> prices = priceDataRepository.findByAssetOrderByDateAsc(asset);

        if (prices.isEmpty()) {
            return ResponseEntity.ok(new SmaResponse(ticker, periods, new ArrayList<>(),
                "No hay datos disponibles para este activo"));
        }

        // Extraer precios de cierre
        List<BigDecimal> closePrices = prices.stream()
                .map(PriceData::getClosePrice)
                .collect(Collectors.toList());

        // Calcular SMA real usando nuestro algoritmo manual
        List<Double> smaValues = simpleMovingAverage.calculate(closePrices, periods);

        // Construir respuesta con fechas alineadas
        List<SmaDataPoint> smaData = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            PriceData price = prices.get(i);
            Double smaValue = (i < smaValues.size()) ? smaValues.get(i) : null;

            if (smaValue != null) {
                smaData.add(new SmaDataPoint(
                    price.getDate(),
                    price.getClosePrice().doubleValue(),
                    smaValue
                ));
            }
        }

        SmaResponse response = new SmaResponse(
            ticker,
            periods,
            smaData,
            "SMA calculada manualmente con ventana deslizante O(n) (SimpleMovingAverage.java)"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Calcula retornos logarítmicos diarios reales para un activo.
     *
     * Fórmula: r_t = ln(P_t / P_{t-1})
     *
     * Usa el algoritmo implementado en ReturnsCalculator.java.
     *
     * @param ticker Símbolo del activo
     * @param startDate Fecha inicial opcional
     * @param endDate Fecha final opcional
     * @return Lista de retornos logarítmicos diarios reales
     */
    @GetMapping("/returns/{ticker}")
    public ResponseEntity<ReturnsResponse> calculateReturns(
            @PathVariable String ticker,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Optional<Asset> assetOpt = assetRepository.findByTicker(ticker.toUpperCase());

        if (assetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Asset asset = assetOpt.get();
        List<PriceData> prices;

        if (startDate != null && endDate != null) {
            prices = priceDataRepository.findByAssetAndDateBetweenOrderByDateAsc(asset, startDate, endDate);
        } else {
            prices = priceDataRepository.findByAssetOrderByDateAsc(asset);
        }

        if (prices.size() < 2) {
            return ResponseEntity.ok(new ReturnsResponse(ticker, new ArrayList<>(), 0.0, 0.0,
                "Se requieren al menos 2 registros de precios para calcular retornos"));
        }

        // Extraer precios de cierre
        List<BigDecimal> closePrices = prices.stream()
                .map(PriceData::getClosePrice)
                .collect(Collectors.toList());

        // Calcular retornos logarítmicos reales
        List<Double> logReturns = returnsCalculator.calculateLogReturns(closePrices);

        // Construir respuesta (el primer valor es null por definición)
        List<ReturnDataPoint> returns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            PriceData price = prices.get(i);
            Double returnValue = (i < logReturns.size()) ? logReturns.get(i) : null;

            if (returnValue != null) {
                returns.add(new ReturnDataPoint(
                    price.getDate(),
                    price.getClosePrice().doubleValue(),
                    returnValue
                ));
            }
        }

        // Calcular estadísticas
        double meanReturn = returns.stream()
                .mapToDouble(ReturnDataPoint::returnValue)
                .average()
                .orElse(0.0);

        double variance = returns.stream()
                .mapToDouble(r -> {
                    double diff = r.returnValue() - meanReturn;
                    return diff * diff;
                })
                .average()
                .orElse(0.0);

        ReturnsResponse response = new ReturnsResponse(
            ticker,
            returns,
            meanReturn,
            variance,
            "Retornos logarítmicos r_t = ln(P_t / P_{t-1}) calculados manualmente (ReturnsCalculator.java)"
        );

        return ResponseEntity.ok(response);
    }

    // ============ DTOs ============

    public record CorrelationMatrixResponse(
        List<String> tickers,
        double[][] matrix,
        String description,
        String notes
    ) {}

    public record SmaResponse(
        String ticker,
        int periods,
        List<SmaDataPoint> data,
        String notes
    ) {}

    public record SmaDataPoint(
        LocalDate date,
        double closePrice,
        double smaValue
    ) {}

    public record ReturnsResponse(
        String ticker,
        List<ReturnDataPoint> returns,
        double meanReturn,
        double variance,
        String notes
    ) {}

    public record ReturnDataPoint(
        LocalDate date,
        double closePrice,
        double returnValue
    ) {}
}
