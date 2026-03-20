package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class PigeonholeSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Pigeonhole Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n + k)";
    }

    @Override
    public String getBestCase() {
        return "O(n + k)";
    }

    @Override
    public String getWorstCase() {
        return "O(n + k)";
    }

    @Override
    public String getAverageCase() {
        return "O(n + k)";
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
        
        // Pigeonhole sort works best with Integers and small ranges
        if (!(array[0] instanceof Number)) {
            insertionSort(array, comparator);
            return;
        }

        int n = array.length;
        
        T min = array[0];
        T max = array[0];
        
        for (int i = 1; i < n; i++) {
            if (comparator.compare(array[i], min) < 0) {
                min = array[i];
            }
            if (comparator.compare(array[i], max) > 0) {
                max = array[i];
            }
        }
        
        long minVal = ((Number) min).longValue();
        long maxVal = ((Number) max).longValue();
        long range = maxVal - minVal + 1;
        
        // Only use Pigeonhole if range is reasonable to avoid OutOfMemoryError
        if (range > 0 && range < 1000000) {
            List<T>[] pigeonholes = new ArrayList[(int) range];
            
            for (int i = 0; i < range; i++) {
                pigeonholes[i] = new ArrayList<>();
            }
            
            for (int i = 0; i < n; i++) {
                int index = (int) (((Number) array[i]).longValue() - minVal);
                pigeonholes[index].add(array[i]);
            }
            
            int index = 0;
            for (int i = 0; i < range; i++) {
                for (T value : pigeonholes[i]) {
                    array[index++] = value;
                }
            }
        } else {
            insertionSort(array, comparator);
        }
    }

    private <T> void insertionSort(T[] array, Comparator<T> comparator) {
        for (int i = 1; i < array.length; i++) {
            T key = array[i];
            int j = i - 1;
            
            while (j >= 0 && comparator.compare(array[j], key) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
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
