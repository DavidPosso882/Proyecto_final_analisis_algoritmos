package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class HeapSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "HeapSort";
    }

    @Override
    public String getComplexity() {
        return "O(n log n)";
    }

    @Override
    public String getBestCase() {
        return "O(n log n)";
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
    public <T> void sort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        int n = array.length;
        
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(array, n, i, comparator);
        }
        
        for (int i = n - 1; i > 0; i--) {
            T temp = array[0];
            array[0] = array[i];
            array[i] = temp;
            
            heapify(array, i, 0, comparator);
        }
    }

    private <T> void heapify(T[] array, int n, int i, Comparator<T> comparator) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        
        if (left < n && comparator.compare(array[left], array[largest]) > 0) {
            largest = left;
        }
        
        if (right < n && comparator.compare(array[right], array[largest]) > 0) {
            largest = right;
        }
        
        if (largest != i) {
            T swap = array[i];
            array[i] = array[largest];
            array[largest] = swap;
            
            heapify(array, n, largest, comparator);
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
