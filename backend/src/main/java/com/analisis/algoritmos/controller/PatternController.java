package com.analisis.algoritmos.controller;

import com.analisis.algoritmos.algorithms.patterns.SlidingWindow;
import com.analisis.algoritmos.algorithms.patterns.VolatilityCalculator;
import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.AssetRepository;
import com.analisis.algoritmos.repository.PriceDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final AssetRepository assetRepository;
    private final PriceDataRepository priceDataRepository;
    private final SlidingWindow slidingWindow;
    private final VolatilityCalculator volatilityCalculator;

    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzePatterns(
            @RequestParam String ticker,
            @RequestParam(defaultValue = "5") int windowSize) {
        
        Asset asset = assetRepository.findByTicker(ticker).orElse(null);
        
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<PriceData> prices = priceDataRepository.findByAssetOrderByDateAsc(asset);
        
        if (prices.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<BigDecimal> closePrices = prices.stream()
            .map(PriceData::getClosePrice)
            .collect(Collectors.toList());
        
        int upwardPatterns = slidingWindow.countUpwardPatterns(closePrices, windowSize);
        int downwardPatterns = slidingWindow.countDownwardPatterns(closePrices, windowSize);
        
        List<Double> localMaxima = slidingWindow.findLocalMaxima(closePrices, windowSize);
        List<Double> localMinima = slidingWindow.findLocalMinima(closePrices, windowSize);
        
        Double volatility = volatilityCalculator.calculateAnnualizedVolatilityFromPrices(closePrices);
        
        Map<String, Object> result = new HashMap<>();
        result.put("ticker", ticker);
        result.put("windowSize", windowSize);
        result.put("totalDataPoints", prices.size());
        result.put("upwardPatterns", upwardPatterns);
        result.put("downwardPatterns", downwardPatterns);
        result.put("localMaxima", localMaxima.size());
        result.put("localMinima", localMinima.size());
        result.put("volatility", volatility != null ? String.format("%.2f%%", volatility * 100) : "N/A");
        result.put("volatilityClassification", volatilityCalculator.classifyVolatility(volatility));
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/volatility")
    public ResponseEntity<Map<String, Object>> getVolatility(
            @RequestParam String ticker) {
        
        Asset asset = assetRepository.findByTicker(ticker).orElse(null);
        
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<PriceData> prices = priceDataRepository.findByAssetOrderByDateAsc(asset);
        
        if (prices.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<BigDecimal> closePrices = prices.stream()
            .map(PriceData::getClosePrice)
            .collect(Collectors.toList());
        
        Double dailyVol = volatilityCalculator.calculateVolatilityFromPrices(closePrices);
        Double annualVol = volatilityCalculator.calculateAnnualizedVolatilityFromPrices(closePrices);
        
        Map<String, Object> result = new HashMap<>();
        result.put("ticker", ticker);
        result.put("dailyVolatility", dailyVol != null ? String.format("%.4f", dailyVol) : "N/A");
        result.put("annualizedVolatility", annualVol != null ? String.format("%.2f%%", annualVol * 100) : "N/A");
        result.put("classification", volatilityCalculator.classifyVolatility(annualVol));
        
        return ResponseEntity.ok(result);
    }
}
