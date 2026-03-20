package com.analisis.algoritmos.controller;

import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.model.PriceDataDTO;
import com.analisis.algoritmos.repository.PriceDataRepository;
import com.analisis.algoritmos.service.SortingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para análisis de algoritmos de ordenamiento.
 */
@RestController
@RequestMapping("/api/sorting")
@RequiredArgsConstructor
public class SortingController {

    private final SortingService sortingService;
    private final PriceDataRepository priceDataRepository;

    @PostMapping("/analyze")
    public ResponseEntity<SortingService.AnalysisResult> analyzeAlgorithms() {
        List<PriceData> allData = priceDataRepository.findAllWithAsset();
        
        if (allData.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        SortingService.AnalysisResult result = sortingService.analyzeAllAlgorithms(allData);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sorted-data")
    public ResponseEntity<List<PriceDataDTO>> getSortedData() {
        List<PriceDataDTO> sorted = sortingService.getAllDataSorted().stream()
            .map(PriceDataDTO::fromEntity)
            .toList();
        
        return ResponseEntity.ok(sorted);
    }

    @GetMapping("/top-volume")
    public ResponseEntity<List<PriceDataDTO>> getTopVolume() {
        List<PriceDataDTO> top = sortingService.getTop15ByVolume().stream()
            .map(PriceDataDTO::fromEntity)
            .toList();
        
        return ResponseEntity.ok(top);
    }
}
