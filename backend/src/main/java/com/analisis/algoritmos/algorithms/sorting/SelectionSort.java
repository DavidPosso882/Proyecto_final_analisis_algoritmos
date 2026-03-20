package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class SelectionSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Selection Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n²)";
    }

    @Override
    public String getBestCase() {
        return "O(n²)";
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
        
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            
            for (int j = i + 1; j < n; j++) {
                if (comparator.compare(array[j], array[minIndex]) < 0) {
                    minIndex = j;
                }
            }
            
            if (minIndex != i) {
                T temp = array[i];
                array[i] = array[minIndex];
                array[minIndex] = temp;
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
