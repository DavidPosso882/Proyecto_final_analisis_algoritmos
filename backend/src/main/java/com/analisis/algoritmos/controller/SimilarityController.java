package com.analisis.algoritmos.controller;

import com.analisis.algoritmos.algorithms.preprocessing.ReturnsCalculator;
import com.analisis.algoritmos.algorithms.similarity.CosineSimilarity;
import com.analisis.algoritmos.algorithms.similarity.DynamicTimeWarping;
import com.analisis.algoritmos.algorithms.similarity.EuclideanDistance;
import com.analisis.algoritmos.algorithms.similarity.PearsonCorrelation;
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
@RequestMapping("/api/similarity")
@RequiredArgsConstructor
public class SimilarityController {

    private final AssetRepository assetRepository;
    private final PriceDataRepository priceDataRepository;
    private final EuclideanDistance euclideanDistance;
    private final PearsonCorrelation pearsonCorrelation;
    private final CosineSimilarity cosineSimilarity;
    private final DynamicTimeWarping dynamicTimeWarping;
    private final ReturnsCalculator returnsCalculator;

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareAssets(
            @RequestParam String ticker1, 
            @RequestParam String ticker2) {
        
        Asset asset1 = assetRepository.findByTicker(ticker1).orElse(null);
        Asset asset2 = assetRepository.findByTicker(ticker2).orElse(null);
        
        if (asset1 == null || asset2 == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<PriceData> prices1 = priceDataRepository.findByAssetOrderByDateAsc(asset1);
        List<PriceData> prices2 = priceDataRepository.findByAssetOrderByDateAsc(asset2);
        
        if (prices1.isEmpty() || prices2.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        int minLength = Math.min(prices1.size(), prices2.size());
        List<BigDecimal> series1 = prices1.stream()
            .limit(minLength)
            .map(PriceData::getClosePrice)
            .collect(Collectors.toList());
        List<BigDecimal> series2 = prices2.stream()
            .limit(minLength)
            .map(PriceData::getClosePrice)
            .collect(Collectors.toList());
        
        List<Double> returns1 = returnsCalculator.calculateLogReturns(series1).stream()
            .filter(r -> r != null)
            .collect(Collectors.toList());
        List<Double> returns2 = returnsCalculator.calculateLogReturns(series2).stream()
            .filter(r -> r != null)
            .collect(Collectors.toList());
        
        int minReturnsLength = Math.min(returns1.size(), returns2.size());
        List<Double> ret1 = returns1.subList(0, minReturnsLength);
        List<Double> ret2 = returns2.subList(0, minReturnsLength);
        
        Map<String, Object> result = new HashMap<>();
        result.put("asset1", ticker1);
        result.put("asset2", ticker2);
        result.put("dataPoints", minLength);
        
        Double euclidean = euclideanDistance.calculate(series1, series2);
        result.put("euclideanDistance", euclidean != null ? euclidean : "N/A");
        
        Double pearson = pearsonCorrelation.calculateWithDouble(ret1, ret2);
        result.put("pearsonCorrelation", pearson != null ? pearson : "N/A");
        
        Double cosine = cosineSimilarity.calculateWithDouble(ret1, ret2);
        result.put("cosineSimilarity", cosine != null ? cosine : "N/A");
        
        Double dtw = dynamicTimeWarping.calculateWithDouble(ret1, ret2);
        result.put("dtw", dtw != null ? dtw : "N/A");
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/euclidean")
    public ResponseEntity<Map<String, Object>> getEuclideanDistance(
            @RequestParam String ticker1, 
            @RequestParam String ticker2) {
        
        return compareTwoAssets(ticker1, ticker2, "euclideanDistance");
    }

    @GetMapping("/pearson")
    public ResponseEntity<Map<String, Object>> getPearsonCorrelation(
            @RequestParam String ticker1, 
            @RequestParam String ticker2) {
        
        return compareTwoAssets(ticker1, ticker2, "pearsonCorrelation");
    }

    @GetMapping("/cosine")
    public ResponseEntity<Map<String, Object>> getCosineSimilarity(
            @RequestParam String ticker1, 
            @RequestParam String ticker2) {
        
        return compareTwoAssets(ticker1, ticker2, "cosineSimilarity");
    }

    @GetMapping("/dtw")
    public ResponseEntity<Map<String, Object>> getDTW(
            @RequestParam String ticker1, 
            @RequestParam String ticker2) {
        
        return compareTwoAssets(ticker1, ticker2, "dtw");
    }

    private ResponseEntity<Map<String, Object>> compareTwoAssets(
            String ticker1, String ticker2, String metric) {
        
        Asset asset1 = assetRepository.findByTicker(ticker1).orElse(null);
        Asset asset2 = assetRepository.findByTicker(ticker2).orElse(null);
        
        if (asset1 == null || asset2 == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<PriceData> prices1 = priceDataRepository.findByAssetOrderByDateAsc(asset1);
        List<PriceData> prices2 = priceDataRepository.findByAssetOrderByDateAsc(asset2);
        
        if (prices1.isEmpty() || prices2.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        int minLength = Math.min(prices1.size(), prices2.size());
        List<BigDecimal> series1 = prices1.stream()
            .limit(minLength)
            .map(PriceData::getClosePrice)
            .collect(Collectors.toList());
        List<BigDecimal> series2 = prices2.stream()
            .limit(minLength)
            .map(PriceData::getClosePrice)
            .collect(Collectors.toList());
        
        List<Double> returns1 = returnsCalculator.calculateLogReturns(series1).stream()
            .filter(r -> r != null)
            .collect(Collectors.toList());
        List<Double> returns2 = returnsCalculator.calculateLogReturns(series2).stream()
            .filter(r -> r != null)
            .collect(Collectors.toList());
        
        int minReturnsLength = Math.min(returns1.size(), returns2.size());
        List<Double> ret1 = returns1.subList(0, minReturnsLength);
        List<Double> ret2 = returns2.subList(0, minReturnsLength);
        
        Map<String, Object> result = new HashMap<>();
        result.put("ticker1", ticker1);
        result.put("ticker2", ticker2);
        
        Object value = switch (metric) {
            case "euclideanDistance" -> euclideanDistance.calculate(series1, series2);
            case "pearsonCorrelation" -> pearsonCorrelation.calculateWithDouble(ret1, ret2);
            case "cosineSimilarity" -> cosineSimilarity.calculateWithDouble(ret1, ret2);
            case "dtw" -> dynamicTimeWarping.calculateWithDouble(ret1, ret2);
            default -> "N/A";
        };
        
        result.put("value", value != null ? value : "N/A");
        
        return ResponseEntity.ok(result);
    }
}
