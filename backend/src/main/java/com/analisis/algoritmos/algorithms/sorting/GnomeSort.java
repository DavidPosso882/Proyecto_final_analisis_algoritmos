package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class GnomeSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Gnome Sort";
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
        
        int index = 0;
        
        while (index < array.length) {
            if (index == 0) {
                index++;
            }
            
            if (comparator.compare(array[index], array[index - 1]) >= 0) {
                index++;
            } else {
                T temp = array[index];
                array[index] = array[index - 1];
                array[index - 1] = temp;
                index--;
            }
        }
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
