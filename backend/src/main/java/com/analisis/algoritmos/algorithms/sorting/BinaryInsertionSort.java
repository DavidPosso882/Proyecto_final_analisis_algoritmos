package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class BinaryInsertionSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Binary Insertion Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n²)";
    }

    @Override
    public String getBestCase() {
        return "O(n log n)";
    }

    @Override
    public String getWorstCase() {
        return "O(n²)";
    }

    @Override
    public String getAverageCase() {
        return "O(n²)";
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
        
        for (int i = 1; i < array.length; i++) {
            T key = array[i];
            int insertIndex = findInsertIndex(array, key, 0, i - 1, comparator);
            
            for (int j = i; j > insertIndex; j--) {
                array[j] = array[j - 1];
            }
            array[insertIndex] = key;
        }
    }

    private <T> int findInsertIndex(T[] array, T key, int low, int high, Comparator<T> comparator) {
        while (low <= high) {
            int mid = low + (high - low) / 2;
            
            if (comparator.compare(array[mid], key) < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        
        return low;
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    public boolean isInPlace() {
        return true;
    }
}
