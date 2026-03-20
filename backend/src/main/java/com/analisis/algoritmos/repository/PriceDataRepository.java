package com.analisis.algoritmos.repository;

import com.analisis.algoritmos.model.Asset;
import com.analisis.algoritmos.model.PriceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de datos de precios.
 * 
 * Proporciona metodos para consultar series temporales de precios
 * con diferentes filtros y rangos de fechas.
 * 
 * @author David
 * @since 1.0.0
 * @see PriceData
 */
@Repository
public interface PriceDataRepository extends JpaRepository<PriceData, Long> {

    /**
     * Busca todos los datos de precios para un activo especifico,
     * ordenados por fecha ascendente.
     * 
     * @param asset Activo a consultar
     * @return Lista de datos de precios ordenados por fecha
     */
    List<PriceData> findByAssetOrderByDateAsc(Asset asset);

    /**
     * Busca datos de precios para un activo en un rango de fechas.
     * 
     * @param asset Activo a consultar
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de datos en el rango especificado
     */
    List<PriceData> findByAssetAndDateBetweenOrderByDateAsc(
            Asset asset, 
            LocalDate startDate, 
            LocalDate endDate
    );

    /**
     * Busca el dato de precio para un activo en una fecha especifica.
     * 
     * @param asset Activo
     * @param date Fecha
     * @return Optional con el dato o vacio
     */
    Optional<PriceData> findByAssetAndDate(Asset asset, LocalDate date);

    /**
     * Busca la fecha del primer registro disponible para un activo.
     * 
     * @param asset Activo
     * @return Fecha mas antigua
     */
    @Query("SELECT MIN(p.date) FROM PriceData p WHERE p.asset = :asset")
    LocalDate findMinDateByAsset(@Param("asset") Asset asset);

    /**
     * Busca la fecha del ultimo registro disponible para un activo.
     * 
     * @param asset Activo
     * @return Fecha mas reciente
     */
    @Query("SELECT MAX(p.date) FROM PriceData p WHERE p.asset = :asset")
    LocalDate findMaxDateByAsset(@Param("asset") Asset asset);

    /**
     * Cuenta el numero de registros para un activo.
     * 
     * @param asset Activo
     * @return Cantidad de registros
     */
    long countByAsset(Asset asset);

    /**
     * Elimina todos los datos de precios para un activo.
     * Util para reconstruir datos de un activo especifico.
     * 
     * @param asset Activo a eliminar
     */
    void deleteByAsset(Asset asset);

    /**
     * Verifica si existe un registro para un activo en una fecha.
     * 
     * @param asset Activo
     * @param date Fecha
     * @return true si existe
     */
    boolean existsByAssetAndDate(Asset asset, LocalDate date);

    /**
     * Busca datos de precios excluyendo dias interpolados.
     * Util para analisis que requieren datos reales.
     * 
     * @param asset Activo
     * @return Lista de datos reales (no interpolados)
     */
    List<PriceData> findByAssetAndInterpolatedFalseOrderByDateAsc(Asset asset);

    /**
     * Busca todos los datos disponibles para una fecha especifica
     * (util para analisis transversales).
     * 
     * @param date Fecha
     * @return Lista de datos de todos los activos para esa fecha
     */
    List<PriceData> findByDate(LocalDate date);

    /**
     * Busca los ultimos N registros para un activo.
     * Util para calculos recientes (SMA, volatilidad).
     * 
     * @param asset Activo
     * @param limit Numero maximo de registros
     * @return Lista con los ultimos N registros
     */
    @Query(value = "SELECT * FROM price_data WHERE asset_id = :assetId ORDER BY date DESC LIMIT :limit", 
           nativeQuery = true)
    List<PriceData> findLastNByAsset(@Param("assetId") Long assetId, @Param("limit") int limit);

    /**
     * Busca todos los datos de precios cargando el activo asociado en una sola consulta.
     * Evita el problema N+1 cuando se accede a las propiedades del activo.
     * 
     * @return Lista de datos de precios con activos cargados
     */
    @Query("SELECT p FROM PriceData p JOIN FETCH p.asset ORDER BY p.date ASC, p.closePrice ASC")
    List<PriceData> findAllWithAsset();

    /**
     * Busca todos los datos de precios ordenados por volumen descendente, cargando el activo asociado.
     * 
     * @return Lista de datos de precios con activos cargados, ordenados por volumen DESC
     */
    @Query("SELECT p FROM PriceData p JOIN FETCH p.asset ORDER BY p.volume DESC")
    List<PriceData> findAllWithAssetOrderByVolumeDesc();
}
