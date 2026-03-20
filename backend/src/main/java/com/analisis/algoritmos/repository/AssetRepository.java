package com.analisis.algoritmos.repository;

import com.analisis.algoritmos.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de activos financieros.
 * 
 * Proporciona metodos para buscar, guardar y gestionar activos
 * en la base de datos.
 * 
 * @author David
 * @since 1.0.0
 * @see Asset
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Busca un activo por su ticker (simbolo).
     * 
     * @param ticker Simbolo del activo (ej: ECOPETROL, VOO)
     * @return Optional con el activo encontrado o vacio
     */
    Optional<Asset> findByTicker(String ticker);

    /**
     * Verifica si existe un activo con el ticker dado.
     * 
     * @param ticker Simbolo a verificar
     * @return true si existe
     */
    boolean existsByTicker(String ticker);

    /**
     * Busca activos por tipo (STOCK o ETF).
     * 
     * @param type Tipo de activo
     * @return Lista de activos del tipo especificado
     */
    List<Asset> findByType(Asset.AssetType type);

    /**
     * Busca activos por mercado.
     * 
     * @param market Mercado (ej: BVC, NYSE, NASDAQ)
     * @return Lista de activos del mercado
     */
    List<Asset> findByMarket(String market);

    /**
     * Busca activos por tipo y mercado.
     * 
     * @param type Tipo de activo
     * @param market Mercado
     * @return Lista filtrada
     */
    List<Asset> findByTypeAndMarket(Asset.AssetType type, String market);

    /**
     * Cuenta el numero total de activos registrados.
     * 
     * @return Cantidad de activos
     */
    long count();

    /**
     * Busca todos los activos ordenados por ticker.
     * 
     * @return Lista ordenada de activos
     */
    List<Asset> findAllByOrderByTickerAsc();

    /**
     * Busca activos cuyo nombre contenga el texto dado (ignora case).
     * 
     * @param name Texto a buscar en el nombre
     * @return Lista de activos coincidentes
     */
    @Query("SELECT a FROM Asset a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Asset> findByNameContainingIgnoreCase(String name);
}
