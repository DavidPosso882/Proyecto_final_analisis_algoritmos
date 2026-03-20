package com.analisis.algoritmos.service;

import com.analisis.algoritmos.algorithms.sorting.QuickSort;
import com.analisis.algoritmos.algorithms.sorting.SortingAlgorithm;
import com.analisis.algoritmos.model.PriceData;
import com.analisis.algoritmos.repository.PriceDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio para análisis comparativo de algoritmos de ordenamiento.
 *
 * Requerimiento 2 del proyecto:
 * - Ejecutar y comparar los 12 algoritmos de ordenamiento sobre datos financieros
 * - Producir la Tabla 1 con nombre, tamaño, complejidad y tiempo
 * - Ordenar registros por fecha + precio de cierre
 * - Obtener los 15 días con mayor volumen ordenados ascendentemente
 *
 * NOTA: Todos los algoritmos son implementaciones manuales propias.
 *
 * @author David
 * @since 1.0.0
 */
@Service
@Slf4j
public class SortingService {

    private final List<SortingAlgorithm> sortingAlgorithms;
    private final PriceDataRepository priceDataRepository;
    private final QuickSort quickSort;

    public SortingService(List<SortingAlgorithm> sortingAlgorithms,
                          PriceDataRepository priceDataRepository,
                          QuickSort quickSort) {
        this.sortingAlgorithms = sortingAlgorithms;
        this.priceDataRepository = priceDataRepository;
        this.quickSort = quickSort;
    }

    public record AlgorithmResult(
        String name,
        String complexity,
        String bestCase,
        String worstCase,
        String averageCase,
        long timeNanos,
        double timeMs,
        boolean isStable,
        boolean isInPlace,
        int dataSize
    ) {}

    public record AnalysisResult(
        List<AlgorithmResult> algorithms,
        int dataSize,
        long totalExecutionTimeMs
    ) {}

    /**
     * Ejecuta los 12 algoritmos de ordenamiento sobre los datos del dataset
     * y produce la Tabla 1 requerida.
     *
     * El benchmark ordena volúmenes de negociación (Long[]) ya que los
     * algoritmos como RadixSort, PigeonholeSort y BucketSort están optimizados
     * para valores numéricos. Adicionalmente, se ejecuta el ordenamiento
     * de registros completos por fecha + cierre con QuickSort como referencia.
     *
     * @param data Lista de datos de precios del dataset unificado
     * @return Resultado del análisis con tiempos por algoritmo
     */
    public AnalysisResult analyzeAllAlgorithms(List<PriceData> data) {
        List<AlgorithmResult> results = new ArrayList<>();

        if (data == null || data.isEmpty()) {
            return new AnalysisResult(results, 0, 0);
        }

        Long[] volumes = data.stream()
            .map(PriceData::getVolume)
            .toArray(Long[]::new);

        int dataSize = volumes.length;
        long totalStartTime = System.currentTimeMillis();

        for (SortingAlgorithm algorithm : sortingAlgorithms) {
            try {
                Long[] testArray = volumes.clone();

                // Warm-up run para estabilizar el JIT
                Long[] warmup = volumes.clone();
                if (warmup.length > 1000) {
                    Long[] smallWarmup = new Long[1000];
                    System.arraycopy(warmup, 0, smallWarmup, 0, 1000);
                    algorithm.sort(smallWarmup);
                }

                long startTime = System.nanoTime();
                algorithm.sort(testArray);
                long endTime = System.nanoTime();

                long timeNanos = endTime - startTime;
                double timeMs = timeNanos / 1_000_000.0;

                results.add(new AlgorithmResult(
                    algorithm.getName(),
                    algorithm.getComplexity(),
                    algorithm.getBestCase(),
                    algorithm.getWorstCase(),
                    algorithm.getAverageCase(),
                    timeNanos,
                    timeMs,
                    algorithm.isStable(),
                    algorithm.isInPlace(),
                    dataSize
                ));

                log.info("{} completado en {} ns ({} ms) - {} registros",
                    algorithm.getName(), timeNanos, timeMs, dataSize);

            } catch (Exception e) {
                log.error("Error ejecutando {}: {}", algorithm.getName(), e.getMessage());
                results.add(new AlgorithmResult(
                    algorithm.getName(),
                    algorithm.getComplexity(),
                    algorithm.getBestCase(),
                    algorithm.getWorstCase(),
                    algorithm.getAverageCase(),
                    -1,
                    -1,
                    algorithm.isStable(),
                    algorithm.isInPlace(),
                    dataSize
                ));
            }
        }

        long totalTime = System.currentTimeMillis() - totalStartTime;

        // Ordenar resultados por tiempo ascendente (Req. 2b)
        results.sort(Comparator.comparingDouble(AlgorithmResult::timeMs));

        return new AnalysisResult(results, dataSize, totalTime);
    }

    /**
     * Ordena todos los registros del dataset unificado por fecha (ascendente)
     * y cuando las fechas coinciden, por precio de cierre (ascendente).
     *
     * Usa QuickSort como algoritmo de referencia con Comparator compuesto.
     * Cumple con Req. 2: "Ordenar de manera ascendente cada uno de los registros
     * a partir del archivo unificado. El ordenamiento se debe realizar teniendo
     * en cuenta la fecha de cotización del activo. Cuando los registros tengan
     * la misma fecha, el siguiente criterio de ordenamiento es el precio de cierre."
     *
     * @param data Lista de datos a ordenar
     * @return Lista ordenada por fecha + precio de cierre
     */
    public List<PriceData> sortByDateAndClosePrice(List<PriceData> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        PriceData[] array = data.toArray(new PriceData[0]);

        Comparator<PriceData> comparator = Comparator
            .comparing(PriceData::getDate)
            .thenComparing(PriceData::getClosePrice);

        quickSort.sort(array, comparator);

        return new ArrayList<>(List.of(array));
    }

    /**
     * Obtiene los 15 días con mayor volumen de negociación para todos los activos,
     * ordenados de manera ascendente por volumen.
     *
     * Estrategia:
     * 1. Seleccionar los 15 registros con mayor volumen usando HeapSort parcial.
     * 2. Ordenar esos 15 registros ascendentemente por volumen con QuickSort propio.
     *
     * Cumple con Req. 2: "Ordenar de manera ascendente los quince primeros
     * días con mayor volumen de negociación para los activos."
     *
     * @return Los 15 registros con mayor volumen, en orden ascendente
     */
    public List<PriceData> getTop15ByVolume() {
        List<PriceData> allData = priceDataRepository.findAllWithAssetOrderByVolumeDesc();

        if (allData.size() <= 15) {
            PriceData[] array = allData.toArray(new PriceData[0]);
            quickSort.sort(array, Comparator.comparing(PriceData::getVolume));
            return new ArrayList<>(List.of(array));
        }

        // Paso 1: Usar QuickSort propio para ordenar todos los datos por volumen desc
        PriceData[] allArray = allData.toArray(new PriceData[0]);
        quickSort.sort(allArray, Comparator.comparing(PriceData::getVolume).reversed());

        // Paso 2: Tomar los primeros 15 (mayor volumen)
        PriceData[] top15 = new PriceData[15];
        System.arraycopy(allArray, 0, top15, 0, 15);

        // Paso 3: Ordenar esos 15 ascendentemente por volumen con QuickSort propio
        quickSort.sort(top15, Comparator.comparing(PriceData::getVolume));

        return new ArrayList<>(List.of(top15));
    }

    /**
     * Obtiene todos los datos del dataset ordenados por fecha + cierre.
     */
    public List<PriceData> getAllDataSorted() {
        List<PriceData> allData = priceDataRepository.findAllWithAsset();
        return sortByDateAndClosePrice(allData);
    }
}
