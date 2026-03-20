package com.analisis.algoritmos.controller;

import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.AssetRepository;
import com.analisis.algoritmos.repository.PriceDataRepository;
import com.analisis.algoritmos.service.EtlService;
import com.analisis.algoritmos.service.EtlService.DownloadSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/etl")
@RequiredArgsConstructor
public class EtlController {

    private final AssetRepository assetRepository;
    private final PriceDataRepository priceDataRepository;
    private final EtlService etlService;

    @GetMapping("/prices/{ticker}")
    public ResponseEntity<List<PriceData>> getPricesByTicker(@PathVariable String ticker) {
        return assetRepository.findByTicker(ticker.toUpperCase())
                .map(asset -> {
                    List<PriceData> prices = priceDataRepository.findByAssetOrderByDateAsc(asset);
                    return ResponseEntity.ok(prices);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/prices/{ticker}/range")
    public ResponseEntity<List<PriceData>> getPricesByRange(
            @PathVariable String ticker,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return assetRepository.findByTicker(ticker.toUpperCase())
                .map(asset -> {
                    List<PriceData> prices = priceDataRepository
                            .findByAssetAndDateBetweenOrderByDateAsc(asset, startDate, endDate);
                    return ResponseEntity.ok(prices);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/prices/{ticker}/range-info")
    public ResponseEntity<DateRangeInfo> getDateRangeInfo(@PathVariable String ticker) {
        return assetRepository.findByTicker(ticker.toUpperCase())
                .map(asset -> {
                    LocalDate minDate = priceDataRepository.findMinDateByAsset(asset);
                    LocalDate maxDate = priceDataRepository.findMaxDateByAsset(asset);
                    long count = priceDataRepository.countByAsset(asset);
                    
                    return ResponseEntity.ok(new DateRangeInfo(minDate, maxDate, count));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/prices/by-date")
    public ResponseEntity<List<PriceData>> getPricesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PriceData> prices = priceDataRepository.findByDate(date);
        return ResponseEntity.ok(prices);
    }

    @PostMapping("/download")
    public ResponseEntity<DownloadSummary> triggerDownload() {
        try {
            DownloadSummary summary = etlService.downloadAllAssets();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rebuild")
    public ResponseEntity<DownloadSummary> rebuildDataset() {
        try {
            DownloadSummary summary = etlService.rebuildDataset();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/test-connection")
    public ResponseEntity<Boolean> testConnection() {
        boolean connected = etlService.testConnection();
        return ResponseEntity.ok(connected);
    }

    public record DateRangeInfo(
        LocalDate minDate,
        LocalDate maxDate,
        long count
    ) {}
}
