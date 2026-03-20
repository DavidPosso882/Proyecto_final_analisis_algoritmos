package com.analisis.algoritmos.controller;

import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para gestion de activos financieros.
 * 
 * Endpoints para consultar, crear y gestionar los activos
 * (acciones y ETFs) disponibles en el sistema.
 * 
 * @author David
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetRepository assetRepository;

    /**
     * Obtiene todos los activos registrados.
     * 
     * @return Lista de activos ordenados por ticker
     */
    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        List<Asset> assets = assetRepository.findAllByOrderByTickerAsc();
        return ResponseEntity.ok(assets);
    }

    /**
     * Obtiene un activo por su ID.
     * 
     * @param id ID del activo
     * @return Activo encontrado o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        return assetRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un activo por su ticker.
     * 
     * @param ticker Simbolo del activo (ej: ECOPETROL, VOO)
     * @return Activo encontrado o 404
     */
    @GetMapping("/ticker/{ticker}")
    public ResponseEntity<Asset> getAssetByTicker(@PathVariable String ticker) {
        return assetRepository.findByTicker(ticker.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Filtra activos por tipo (STOCK o ETF).
     * 
     * @param type Tipo de activo
     * @return Lista de activos del tipo especificado
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Asset>> getAssetsByType(@PathVariable Asset.AssetType type) {
        List<Asset> assets = assetRepository.findByType(type);
        return ResponseEntity.ok(assets);
    }

    /**
     * Filtra activos por mercado.
     * 
     * @param market Mercado (ej: BVC, NYSE, NASDAQ)
     * @return Lista de activos del mercado
     */
    @GetMapping("/market/{market}")
    public ResponseEntity<List<Asset>> getAssetsByMarket(@PathVariable String market) {
        List<Asset> assets = assetRepository.findByMarket(market.toUpperCase());
        return ResponseEntity.ok(assets);
    }

    /**
     * Crea un nuevo activo.
     * 
     * @param asset Datos del activo a crear
     * @return Activo creado
     */
    @PostMapping
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        if (assetRepository.existsByTicker(asset.getTicker())) {
            return ResponseEntity.badRequest().build();
        }
        Asset saved = assetRepository.save(asset);
        return ResponseEntity.ok(saved);
    }

    /**
     * Busca activos por nombre (contiene).
     * 
     * @param name Texto a buscar
     * @return Lista de activos coincidentes
     */
    @GetMapping("/search")
    public ResponseEntity<List<Asset>> searchAssets(@RequestParam String name) {
        List<Asset> assets = assetRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(assets);
    }
}
