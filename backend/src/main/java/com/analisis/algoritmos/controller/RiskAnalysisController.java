package com.analisis.algoritmos.controller;

import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.AssetRepository;
import com.analisis.algoritmos.repository.PriceDataRepository;
import com.analisis.algoritmos.algorithms.patterns.VolatilityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
@Slf4j
public class RiskAnalysisController {

    private final AssetRepository assetRepository;
    private final PriceDataRepository priceDataRepository;
    private final VolatilityCalculator volatilityCalculator;

    @GetMapping("/volatility")
    public ResponseEntity<List<AssetRiskDTO>> getAssetVolatility() {
        try {
            List<Asset> assets = assetRepository.findAll();
            List<AssetRiskDTO> result = new ArrayList<>();

            for (Asset asset : assets) {
                // Obtener datos de precios para el activo
                List<PriceData> priceDataList = priceDataRepository.findByAssetOrderByDateAsc(asset);
                
                if (priceDataList.size() < 2) {
                    log.warn("Insuficientes datos para {}: {}", asset.getTicker(), priceDataList.size());
                    continue;
                }

                // Calcular volatilidad anualizada
                List<BigDecimal> prices = priceDataList.stream()
                    .map(PriceData::getClosePrice)
                    .collect(Collectors.toList());

                Double annualizedVolatility = volatilityCalculator.calculateAnnualizedVolatilityFromPrices(prices);
                
                if (annualizedVolatility != null) {
                    String riskCategory = volatilityCalculator.classifyVolatility(annualizedVolatility);
                    
                    result.add(new AssetRiskDTO(
                        asset.getTicker(),
                        asset.getName(),
                        asset.getMarket(),
                        annualizedVolatility * 100, // Convertir a porcentaje
                        riskCategory
                    ));
                }
            }

            // Ordenar por volatilidad descendente
            result.sort((a, b) -> Double.compare(b.volatility(), a.volatility()));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error calculando volatilidad", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public record AssetRiskDTO(
        String ticker,
        String name,
        String market,
        Double volatility,
        String riskCategory
    ) {}
}