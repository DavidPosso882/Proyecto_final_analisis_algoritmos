package com.analisis.algoritmos.algorithms.sorting;

public interface SortingAlgorithm {
    String getName();
    String getComplexity();
    String getBestCase();
    String getWorstCase();
    String getAverageCase();
    <T extends Comparable<T>> void sort(T[] array);
    <T> void sort(T[] array, java.util.Comparator<T> comparator);
    boolean isStable();
    boolean isInPlace();
}
