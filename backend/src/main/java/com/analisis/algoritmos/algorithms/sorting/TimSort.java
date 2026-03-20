package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Implementación manual del algoritmo TimSort.
 *
 * TimSort es un algoritmo híbrido que combina Merge Sort e Insertion Sort.
 * Fue diseñado por Tim Peters para Python y es el algoritmo por defecto
 * en Java (Arrays.sort) y Python (sorted/list.sort).
 *
 * Estrategia:
 * 1. Dividir el array en "runs" (sub-secuencias) de tamaño mínimo MIN_RUN.
 * 2. Ordenar cada run con Insertion Sort (eficiente para arrays pequeños).
 * 3. Fusionar los runs usando un merge iterativo estilo bottom-up.
 *
 * Complejidad:
 * - Mejor caso: O(n) — cuando el array ya está casi ordenado (pocos runs).
 * - Caso promedio: O(n log n)
 * - Peor caso: O(n log n)
 * - Espacio: O(n) — por el array temporal de merge.
 *
 * @author David
 * @since 1.0.0
 */
@Component
public class TimSort implements SortingAlgorithm {

    /**
     * Tamaño mínimo de un "run". Los runs más pequeños se extienden
     * a MIN_RUN elementos usando Insertion Sort.
     * El valor 32 es el estándar usado en CPython y OpenJDK.
     */
    private static final int MIN_RUN = 32;

    @Override
    public String getName() {
        return "TimSort";
    }

    @Override
    public String getComplexity() {
        return "O(n log n)";
    }

    @Override
    public String getBestCase() {
        return "O(n)";
    }

    @Override
    public String getWorstCase() {
        return "O(n log n)";
    }

    @Override
    public String getAverageCase() {
        return "O(n log n)";
    }

    @Override
    public <T extends Comparable<T>> void sort(T[] array) {
        sort(array, Comparator.naturalOrder());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void sort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length <= 1) {
            return;
        }

        int n = array.length;

        // Paso 1: Ordenar runs individuales con Insertion Sort
        for (int i = 0; i < n; i += MIN_RUN) {
            int end = Math.min(i + MIN_RUN - 1, n - 1);
            insertionSort(array, i, end, comparator);
        }

        // Paso 2: Fusionar runs de abajo hacia arriba (bottom-up merge)
        // Empezamos con runs de tamaño MIN_RUN y los duplicamos en cada iteración
        T[] temp = (T[]) new Object[n];

        for (int size = MIN_RUN; size < n; size *= 2) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = Math.min(left + size - 1, n - 1);
                int right = Math.min(left + 2 * size - 1, n - 1);

                // Solo fusionar si hay dos runs adyacentes para fusionar
                if (mid < right) {
                    merge(array, temp, left, mid, right, comparator);
                }
            }
        }
    }

    /**
     * Insertion Sort para un sub-rango del array [left, right].
     * Es eficiente para arrays pequeños (< ~64 elementos) y para
     * datos parcialmente ordenados, lo cual es la base del TimSort.
     *
     * Complejidad: O(k²) donde k = right - left + 1
     * Para k ≤ MIN_RUN, esto es O(MIN_RUN²) = O(1) constante.
     */
    private <T> void insertionSort(T[] array, int left, int right, Comparator<T> comparator) {
        for (int i = left + 1; i <= right; i++) {
            T key = array[i];
            int j = i - 1;

            while (j >= left && comparator.compare(array[j], key) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    /**
     * Merge de dos sub-arrays ordenados: [left..mid] y [mid+1..right].
     * Usa un array temporal para almacenar la copia y luego fusiona
     * in-place de vuelta al array original.
     *
     * Complejidad: O(right - left + 1) en tiempo y espacio.
     */
    private <T> void merge(T[] array, T[] temp, int left, int mid, int right, Comparator<T> comparator) {
        // Copiar ambos sub-arrays al array temporal
        for (int k = left; k <= right; k++) {
            temp[k] = array[k];
        }

        int i = left;       // Índice para el sub-array izquierdo
        int j = mid + 1;    // Índice para el sub-array derecho
        int k = left;       // Índice para el array resultado

        // Fusionar comparando elemento a elemento
        while (i <= mid && j <= right) {
            // Usar <= para mantener estabilidad (elementos iguales del izquierdo van primero)
            if (comparator.compare(temp[i], temp[j]) <= 0) {
                array[k++] = temp[i++];
            } else {
                array[k++] = temp[j++];
            }
        }

        // Copiar elementos restantes del sub-array izquierdo
        while (i <= mid) {
            array[k++] = temp[i++];
        }

        // Los elementos restantes del derecho ya están en posición correcta
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    public boolean isInPlace() {
        return false;
    }
}
