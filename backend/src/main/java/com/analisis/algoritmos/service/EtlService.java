package com.analisis.algoritmos.service;

import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.AssetRepository;
import com.analisis.algoritmos.repository.PriceDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtlService {

    private final AssetRepository assetRepository;
    private final PriceDataRepository priceDataRepository;
    private final YahooFinanceClient yahooFinanceClient;

    @Value("${etl.request.delay:1000}")
    private long requestDelay;

    @Value("${etl.max.retries:3}")
    private int maxRetries;

    @Value("${etl.years.of.data:5}")
    private int yearsOfData;

    private static final List<AssetConfig> INITIAL_ASSETS = List.of(
        new AssetConfig("ECOPETROL", "Ecopetrol S.A.", Asset.AssetType.STOCK, "BVC", "COP", "Energia"),
        new AssetConfig("ISA", "ISA S.A.", Asset.AssetType.STOCK, "BVC", "COP", "Utilities"),
        new AssetConfig("GEB", "Grupo Energia Bogota", Asset.AssetType.STOCK, "BVC", "COP", "Utilities"),
        new AssetConfig("PFBCOLOM", "Bancolombia Preferencial", Asset.AssetType.STOCK, "BVC", "COP", "Bancos"),
        new AssetConfig("GRUPOARGOS", "Grupo Argos S.A.", Asset.AssetType.STOCK, "BVC", "COP", "Construccion"),
        new AssetConfig("GRUPOSURA", "Grupo Sura S.A.", Asset.AssetType.STOCK, "BVC", "COP", "Finanzas"),
        new AssetConfig("CEMARGOS", "Cementos Argos", Asset.AssetType.STOCK, "BVC", "COP", "Materiales"),
        new AssetConfig("PFAVAL", "Grupo Aval Acciones y Valores", Asset.AssetType.STOCK, "BVC", "COP", "Bancos"),
        new AssetConfig("ICOLCAP", "Indice Colcap", Asset.AssetType.STOCK, "BVC", "COP", "Indice"),
        new AssetConfig("CNEC", "Cementos Nacionales", Asset.AssetType.STOCK, "BVC", "COP", "Materiales"),
        new AssetConfig("VOO", "Vanguard S&P 500 ETF", Asset.AssetType.ETF, "NYSE", "USD", "Indices"),
        new AssetConfig("QQQ", "Invesco QQQ Trust", Asset.AssetType.ETF, "NASDAQ", "USD", "Indices"),
        new AssetConfig("IWM", "iShares Russell 2000 ETF", Asset.AssetType.ETF, "NYSE", "USD", "Indices"),
        new AssetConfig("VEA", "Vanguard FTSE Developed Markets ETF", Asset.AssetType.ETF, "NYSE", "USD", "Internacional"),
        new AssetConfig("VWO", "Vanguard Emerging Markets ETF", Asset.AssetType.ETF, "NYSE", "USD", "Emergentes"),
        new AssetConfig("BND", "Vanguard Total Bond Market ETF", Asset.AssetType.ETF, "NASDAQ", "USD", "Bonos"),
        new AssetConfig("GLD", "SPDR Gold Shares", Asset.AssetType.ETF, "NYSE", "USD", "Materias Primas"),
        new AssetConfig("XLF", "Financial Select Sector SPDR", Asset.AssetType.ETF, "NYSE", "USD", "Finanzas"),
        new AssetConfig("XLK", "Technology Select Sector SPDR", Asset.AssetType.ETF, "NYSE", "USD", "Tecnologia"),
        new AssetConfig("XLE", "Energy Select Sector SPDR", Asset.AssetType.ETF, "NYSE", "USD", "Energia"),
        new AssetConfig("XLV", "Health Care Select Sector SPDR", Asset.AssetType.ETF, "NYSE", "USD", "Salud"),
        new AssetConfig("VTI", "Vanguard Total Stock Market ETF", Asset.AssetType.ETF, "NYSE", "USD", "Total Market")
    );

    @Transactional
    public void initializeAssets() {
        log.info("Inicializando activos base...");
        
        int created = 0;
        for (AssetConfig config : INITIAL_ASSETS) {
            if (!assetRepository.existsByTicker(config.ticker())) {
                Asset asset = new Asset();
                asset.setTicker(config.ticker());
                asset.setName(config.name());
                asset.setType(config.type());
                asset.setMarket(config.market());
                asset.setCurrency(config.currency());
                asset.setSector(config.sector());
                
                assetRepository.save(asset);
                created++;
                log.info("Creado activo: {}", config.ticker());
            }
        }
        
        log.info("Inicializacion completada. Activos creados: {}", created);
    }

    @Transactional
    public DownloadSummary downloadAllAssets() {
        log.info("Iniciando descarga de todos los activos desde Yahoo Finance...");
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(yearsOfData);
        
        List<Asset> assets = assetRepository.findAll();
        int success = 0;
        int failed = 0;
        int totalRecords = 0;
        List<String> errors = new ArrayList<>();

        for (Asset asset : assets) {
            try {
                log.info("Descargando datos para: {}", asset.getTicker());
                
                List<PriceData> downloadedData = downloadAssetData(asset.getTicker(), startDate, endDate);
                
                if (!downloadedData.isEmpty()) {
                    downloadedData = cleanAndValidateData(downloadedData, asset);
                    downloadedData = fillMissingDates(downloadedData, asset, startDate, endDate);
                    
                    priceDataRepository.saveAll(downloadedData);
                    totalRecords += downloadedData.size();
                    success++;
                    log.info("Guardados {} registros para {}", downloadedData.size(), asset.getTicker());
                } else {
                    failed++;
                    errors.add(asset.getTicker() + ": No se recibieron datos");
                }
                
                Thread.sleep(requestDelay);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Descarga interrumpida para {}", asset.getTicker());
                break;
            } catch (Exception e) {
                failed++;
                errors.add(asset.getTicker() + ": " + e.getMessage());
                log.error("Error descargando {}: {}", asset.getTicker(), e.getMessage());
            }
        }

        log.info("Descarga completada. Exitosos: {}, Fallidos: {}, Total registros: {}", 
            success, failed, totalRecords);
        
        return new DownloadSummary(assets.size(), success, failed, totalRecords, errors);
    }

    public List<PriceData> downloadAssetData(String ticker, LocalDate startDate, LocalDate endDate) {
        YahooFinanceClient.YahooChartResponse response = 
            yahooFinanceClient.downloadHistoricalData(ticker, startDate, endDate);
        
        Map<String, PriceData> pricesMap = response.prices();
        
        return pricesMap.values().stream()
            .sorted(Comparator.comparing(PriceData::getDate))
            .collect(Collectors.toList());
    }

    private List<PriceData> cleanAndValidateData(List<PriceData> data, Asset asset) {
        List<PriceData> cleaned = new ArrayList<>();
        
        for (PriceData price : data) {
            if (isValidPrice(price)) {
                price.setAsset(asset);
                cleaned.add(price);
            } else {
                log.warn("Precio invalido para {} en fecha {} - omitiendo", 
                    asset.getTicker(), price.getDate());
            }
        }
        
        cleaned = detectAndHandleAnomalies(cleaned);
        
        return cleaned;
    }

    private boolean isValidPrice(PriceData price) {
        if (price.getOpenPrice() == null || price.getHighPrice() == null || 
            price.getLowPrice() == null || price.getClosePrice() == null) {
            return false;
        }
        
        if (price.getOpenPrice().compareTo(BigDecimal.ZERO) <= 0 ||
            price.getHighPrice().compareTo(BigDecimal.ZERO) <= 0 ||
            price.getLowPrice().compareTo(BigDecimal.ZERO) <= 0 ||
            price.getClosePrice().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (price.getHighPrice().compareTo(price.getLowPrice()) < 0 ||
            price.getOpenPrice().compareTo(price.getHighPrice()) > 0 ||
            price.getOpenPrice().compareTo(price.getLowPrice()) < 0 ||
            price.getClosePrice().compareTo(price.getHighPrice()) > 0 ||
            price.getClosePrice().compareTo(price.getLowPrice()) < 0) {
            return false;
        }
        
        return true;
    }

    private List<PriceData> detectAndHandleAnomalies(List<PriceData> data) {
        if (data.size() < 2) return data;
        
        List<PriceData> result = new ArrayList<>();
        
        for (int i = 0; i < data.size(); i++) {
            PriceData current = data.get(i);
            
            if (i > 0 && i < data.size() - 1) {
                PriceData prev = data.get(i - 1);
                PriceData next = data.get(i + 1);
                
                BigDecimal prevClose = prev.getClosePrice();
                BigDecimal currentClose = current.getClosePrice();
                
                BigDecimal changePercent = currentClose.subtract(prevClose)
                    .divide(prevClose, 4, RoundingMode.HALF_UP)
                    .abs()
                    .multiply(BigDecimal.valueOf(100));
                
                if (changePercent.compareTo(BigDecimal.valueOf(50)) > 0) {
                    BigDecimal avgClose = prevClose.add(next.getClosePrice())
                        .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                    
                    current.setClosePrice(avgClose);
                    current.setHighPrice(avgClose.multiply(BigDecimal.valueOf(1.01)));
                    current.setLowPrice(avgClose.multiply(BigDecimal.valueOf(0.99)));
                    current.setOpenPrice(avgClose);
                    log.warn("Anomalia corregida para {} en fecha {}", 
                        current.getAsset().getTicker(), current.getDate());
                }
            }
            
            result.add(current);
        }
        
        return result;
    }

    private List<PriceData> fillMissingDates(List<PriceData> data, Asset asset, 
                                              LocalDate startDate, LocalDate endDate) {
        if (data.isEmpty()) return data;
        
        Map<LocalDate, PriceData> dataMap = data.stream()
            .collect(Collectors.toMap(PriceData::getDate, p -> p, (a, b) -> a));
        
        List<PriceData> result = new ArrayList<>();
        LocalDate current = startDate;
        
        PriceData lastValid = null;
        
        while (!current.isAfter(endDate)) {
            // Saltar fines de semana — no son días de negociación
            if (isWeekend(current)) {
                current = current.plusDays(1);
                continue;
            }
            
            if (dataMap.containsKey(current)) {
                lastValid = dataMap.get(current);
                result.add(lastValid);
            } else if (lastValid != null) {
                // Solo interpolar días hábiles faltantes (festivos, etc.)
                PriceData interpolated = createInterpolatedPrice(lastValid, current, asset);
                interpolated.setInterpolated(true);
                interpolated.setNonTradingDay(true);
                result.add(interpolated);
            }
            
            current = current.plusDays(1);
        }
        
        return result;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() > 5;
    }

    private PriceData createInterpolatedPrice(PriceData lastValid, LocalDate targetDate, Asset asset) {
        PriceData interpolated = new PriceData();
        interpolated.setAsset(asset);
        interpolated.setDate(targetDate);
        interpolated.setOpenPrice(lastValid.getClosePrice());
        interpolated.setClosePrice(lastValid.getClosePrice());
        interpolated.setHighPrice(lastValid.getClosePrice());
        interpolated.setLowPrice(lastValid.getClosePrice());
        interpolated.setVolume(0L);
        return interpolated;
    }

    @Transactional
    public DownloadSummary rebuildDataset() {
        log.warn("Reconstruyendo dataset completo...");
        
        priceDataRepository.deleteAll();
        log.info("Datos anteriores eliminados");
        
        return downloadAllAssets();
    }

    public boolean testConnection() {
        return yahooFinanceClient.testConnection();
    }

    public record AssetConfig(
        String ticker,
        String name,
        Asset.AssetType type,
        String market,
        String currency,
        String sector
    ) {}

    public record DownloadSummary(
        int total,
        int success,
        int failed,
        int totalRecords,
        List<String> errors
    ) {
        public boolean isComplete() {
            return failed == 0;
        }
    }
}
