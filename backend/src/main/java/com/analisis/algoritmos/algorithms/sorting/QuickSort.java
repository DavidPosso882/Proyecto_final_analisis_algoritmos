package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class QuickSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "QuickSort";
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
        return "O(n²)";
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
        quickSort(array, 0, array.length - 1, comparator);
    }

    private <T> void quickSort(T[] array, int low, int high, Comparator<T> comparator) {
        // Usar una pila explícita para evitar StackOverflowError
        java.util.Stack<int[]> stack = new java.util.Stack<>();
        stack.push(new int[]{low, high});
        
        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int currentLow = range[0];
            int currentHigh = range[1];
            
            if (currentLow < currentHigh) {
                int pivotIndex = partition(array, currentLow, currentHigh, comparator);
                
                // Empilar el subarray más pequeño primero para limitar la profundidad
                if (pivotIndex - currentLow < currentHigh - pivotIndex) {
                    stack.push(new int[]{currentLow, pivotIndex - 1});
                    stack.push(new int[]{pivotIndex + 1, currentHigh});
                } else {
                    stack.push(new int[]{pivotIndex + 1, currentHigh});
                    stack.push(new int[]{currentLow, pivotIndex - 1});
                }
            }
        }
    }

    private <T> int partition(T[] array, int low, int high, Comparator<T> comparator) {
        T pivot = array[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (comparator.compare(array[j], pivot) <= 0) {
                i++;
                swap(array, i, j);
            }
        }
        
        swap(array, i + 1, high);
        return i + 1;
    }

    private <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
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
