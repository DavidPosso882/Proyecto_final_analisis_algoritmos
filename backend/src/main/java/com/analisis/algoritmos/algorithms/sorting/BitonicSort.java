package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;


import java.util.Comparator;

/**
 * Implementación del algoritmo Bitonic Sort.
 *
 * Bitonic Sort es un algoritmo de ordenamiento paralelo basado en la
 * construcción de secuencias bitónicas y su posterior fusión.
 *
 * Una secuencia bitónica es aquella que primero crece y luego decrece,
 * o que puede rotarse cíclicamente para obtener tal propiedad.
 *
 * NOTA: Bitonic Sort requiere que el tamaño del array sea potencia de 2.
 * Para arrays de tamaño arbitrario, se realiza padding con valores centinela
 * y se eliminan al final.
 *
 * Complejidad:
 * - Mejor, promedio y peor caso: O(n log² n)
 * - Espacio: O(n) en el peor caso (por padding)
 *
 * @author David
 * @since 1.0.0
 */
@Component
public class BitonicSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Bitonic Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n log² n)";
    }

    @Override
    public String getBestCase() {
        return "O(n log² n)";
    }

    @Override
    public String getWorstCase() {
        return "O(n log² n)";
    }

    @Override
    public String getAverageCase() {
        return "O(n log² n)";
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

        if (isPowerOfTwo(n)) {
            // Caso ideal: tamaño es potencia de 2
            bitonicSort(array, 0, n, 1, comparator);
        } else {
            // Estrategia híbrida: usar BitonicSort para la parte potencia de 2
            // y MergeSort para el resto (más eficiente que padding completo)
            int powerOfTwo = largestPowerOfTwoLessOrEqual(n);
            
            // Ordenar la primera parte (potencia de 2) con BitonicSort
            bitonicSort(array, 0, powerOfTwo, 1, comparator);
            
            // Ordenar el resto con MergeSort (ya que no es potencia de 2)
            if (powerOfTwo < n) {
                mergeSort(array, powerOfTwo, n - 1, comparator);
                
                // Fusionar las dos partes ordenadas
                merge(array, 0, powerOfTwo - 1, n - 1, comparator);
            }
        }
    }

    /**
     * Verifica si n es potencia de 2.
     */
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    /**
     * Calcula la siguiente potencia de 2 >= n.
     */
    private int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power <<= 1;
        }
        return power;
    }

    /**
     * Calcula la mayor potencia de 2 menor o igual a n.
     */
    private int largestPowerOfTwoLessOrEqual(int n) {
        int power = 1;
        while (power <= n / 2) {
            power <<= 1;
        }
        return power;
    }

    /**
     * MergeSort para ordenar el resto del array.
     */
    private <T> void mergeSort(T[] array, int left, int right, Comparator<T> comparator) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(array, left, mid, comparator);
            mergeSort(array, mid + 1, right, comparator);
            merge(array, left, mid, right, comparator);
        }
    }

    /**
     * Fusiona dos subarrays ordenados.
     */
    private <T> void merge(T[] array, int left, int mid, int right, Comparator<T> comparator) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        @SuppressWarnings("unchecked")
        T[] leftArray = (T[]) new Object[n1];
        @SuppressWarnings("unchecked")
        T[] rightArray = (T[]) new Object[n2];

        System.arraycopy(array, left, leftArray, 0, n1);
        System.arraycopy(array, mid + 1, rightArray, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (comparator.compare(leftArray[i], rightArray[j]) <= 0) {
                array[k++] = leftArray[i++];
            } else {
                array[k++] = rightArray[j++];
            }
        }

        while (i < n1) {
            array[k++] = leftArray[i++];
        }

        while (j < n2) {
            array[k++] = rightArray[j++];
        }
    }

    /**
     * Ordena recursivamente una secuencia bitónica.
     *
     * @param array Array a ordenar
     * @param low Índice inferior
     * @param count Número de elementos
     * @param dir Dirección: 1 = ascendente, 0 = descendente
     * @param comparator Comparador
     */
    private <T> void bitonicSort(T[] array, int low, int count, int dir, Comparator<T> comparator) {
        if (count > 1) {
            int k = count / 2;

            // Ordenar primera mitad en orden ascendente
            bitonicSort(array, low, k, 1, comparator);
            // Ordenar segunda mitad en orden descendente
            bitonicSort(array, low + k, k, 0, comparator);

            // Fusionar la secuencia bitónica resultante
            bitonicMerge(array, low, count, dir, comparator);
        }
    }

    /**
     * Fusiona (merge) una secuencia bitónica en la dirección especificada.
     */
    private <T> void bitonicMerge(T[] array, int low, int count, int dir, Comparator<T> comparator) {
        if (count > 1) {
            int k = count / 2;

            for (int i = low; i < low + k; i++) {
                compareAndSwap(array, i, i + k, dir, comparator);
            }

            bitonicMerge(array, low, k, dir, comparator);
            bitonicMerge(array, low + k, k, dir, comparator);
        }
    }

    /**
     * Compara dos elementos y los intercambia si no están en el orden correcto.
     *
     * @param dir 1 = ascendente (swap si array[i] > array[j]),
     *            0 = descendente (swap si array[i] < array[j])
     */
    private <T> void compareAndSwap(T[] array, int i, int j, int dir, Comparator<T> comparator) {
        boolean needsSwap;

        if (dir == 1) {
            needsSwap = comparator.compare(array[i], array[j]) > 0;
        } else {
            needsSwap = comparator.compare(array[i], array[j]) < 0;
        }

        if (needsSwap) {
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isInPlace() {
        return true;
    }
}
