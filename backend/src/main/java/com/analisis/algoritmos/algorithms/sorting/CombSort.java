package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class CombSort implements SortingAlgorithm {

    private static final double SHRINK_FACTOR = 1.247330595801023;

    @Override
    public String getName() {
        return "Comb Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n²)";
    }

    @Override
    public String getBestCase() {
        return "O(n)";
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
        
        int n = array.length;
        int gap = n;
        boolean swapped = true;
        
        while (gap > 1 || swapped) {
            gap = (int) (gap / SHRINK_FACTOR);
            if (gap < 1) {
                gap = 1;
            }
            
            swapped = false;
            
            for (int i = 0; i + gap < n; i++) {
                if (comparator.compare(array[i], array[i + gap]) > 0) {
                    T temp = array[i];
                    array[i] = array[i + gap];
                    array[i + gap] = temp;
                    swapped = true;
                }
            }
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
