package com.analisis.algoritmos.config;

import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.AssetRepository;
import com.analisis.algoritmos.repository.PriceDataRepository;
import com.analisis.algoritmos.service.EtlService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final AssetRepository assetRepository;
    private final PriceDataRepository priceDataRepository;
    private final EtlService etlService;
    
    private final Random random = new Random(42);
    
    @Value("${etl.use.real.data:true}")
    private boolean useRealData;
    
    @Value("${etl.fallback.to.mock:true}")
    private boolean fallbackToMock;

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

    @PostConstruct
    @Transactional
    public void initialize() {
        log.info("Iniciando carga de datos...");
        
        try {
            initializeAssets();
            
            long priceDataCount = priceDataRepository.count();
            
            if (priceDataCount > 0) {
                log.info("Ya existen {} registros de precios. Saltando carga de datos.", priceDataCount);
                return;
            }
            
            log.info("No hay datos de precios. Iniciando descarga...");
            
            if (useRealData) {
                log.info("Intentando descargar datos REALES desde Yahoo Finance...");
                
                try {
                    boolean connectionOk = etlService.testConnection();
                    
                    if (connectionOk) {
                        EtlService.DownloadSummary summary = etlService.downloadAllAssets();
                        
                        if (summary.isComplete()) {
                            log.info("Descarga REAL completada exitosamente: {} registros", summary.totalRecords());
                            return;
                        } else {
                            log.warn("Descarga real parcialmente exitosa. Exitosos: {}/{}, Fallidos: {}", 
                                summary.success(), summary.total(), summary.failed());
                            
                            if (!fallbackToMock || summary.totalRecords() < 1000) {
                                log.info("Usando datos mock como fallback...");
                                initializeMockPriceData();
                            }
                        }
                    } else {
                        log.warn("No se pudo conectar a Yahoo Finance. Verificando si usar fallback...");
                        if (fallbackToMock) {
                            initializeMockPriceData();
                        }
                    }
                } catch (Exception e) {
                    log.error("Error durante descarga real: {}. {}", e.getMessage(), 
                        fallbackToMock ? "Usando datos mock..." : "Sin datos disponibles");
                    if (fallbackToMock) {
                        initializeMockPriceData();
                    }
                }
            } else {
                log.info("Configurado para usar datos MOCK. Generando...");
                initializeMockPriceData();
            }
            
            log.info("Carga de datos completada");
            
        } catch (Exception e) {
            log.error("Error durante la inicializacion de datos: {}", e.getMessage(), e);
        }
    }

    private void initializeAssets() {
        long existingCount = assetRepository.count();
        
        if (existingCount > 0) {
            log.info("Ya existen {} activos en la base de datos. Saltando inicializacion.", existingCount);
            return;
        }
        
        log.info("Cargando {} activos...", INITIAL_ASSETS.size());
        
        int created = 0;
        for (AssetConfig config : INITIAL_ASSETS) {
            Asset asset = new Asset();
            asset.setTicker(config.ticker());
            asset.setName(config.name());
            asset.setType(config.type());
            asset.setMarket(config.market());
            asset.setCurrency(config.currency());
            asset.setSector(config.sector());
            
            assetRepository.save(asset);
            created++;
        }
        
        log.info("Activos creados exitosamente: {}", created);
    }

    private void initializeMockPriceData() {
        log.info("Generando datos OHLCV mock (5 anos por activo)...");
        
        List<Asset> assets = assetRepository.findAll();
        int totalRecords = 0;
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(5);
        
        for (Asset asset : assets) {
            int recordsForAsset = generateMockPriceDataForAsset(asset, startDate, endDate);
            totalRecords += recordsForAsset;
            log.debug("Generados {} registros para {}", recordsForAsset, asset.getTicker());
        }
        
        log.info("Total de mock generados: {}", totalRecords);
    }

    private int generateMockPriceDataForAsset(Asset asset, LocalDate startDate, LocalDate endDate) {
        int count = 0;
        LocalDate currentDate = startDate;
        
        double basePrice = asset.getType() == Asset.AssetType.STOCK ? 
            (asset.getMarket().equals("BVC") ? 5000.0 : 50.0) : 100.0;
        
        double lastClose = basePrice;
        
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek().getValue() > 5) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            double variation = (random.nextDouble() - 0.5) * 0.04;
            double open = lastClose * (1 + (random.nextDouble() - 0.5) * 0.01);
            double close = open * (1 + variation);
            double high = Math.max(open, close) * (1 + random.nextDouble() * 0.02);
            double low = Math.min(open, close) * (1 - random.nextDouble() * 0.02);
            long volume = 100000 + random.nextLong(900000);
            
            PriceData priceData = new PriceData();
            priceData.setAsset(asset);
            priceData.setDate(currentDate);
            priceData.setOpenPrice(BigDecimal.valueOf(open));
            priceData.setHighPrice(BigDecimal.valueOf(high));
            priceData.setLowPrice(BigDecimal.valueOf(low));
            priceData.setClosePrice(BigDecimal.valueOf(close));
            priceData.setVolume(volume);
            priceData.setInterpolated(false);
            priceData.setNonTradingDay(false);
            
            priceDataRepository.save(priceData);
            
            lastClose = close;
            count++;
            currentDate = currentDate.plusDays(1);
        }
        
        return count;
    }

    private record AssetConfig(
        String ticker,
        String name,
        Asset.AssetType type,
        String market,
        String currency,
        String sector
    ) {}
}
