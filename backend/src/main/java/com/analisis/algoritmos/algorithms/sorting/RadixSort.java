package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class RadixSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "RadixSort";
    }

    @Override
    public String getComplexity() {
        return "O(nk)";
    }

    @Override
    public String getBestCase() {
        return "O(nk)";
    }

    @Override
    public String getWorstCase() {
        return "O(nk)";
    }

    @Override
    public String getAverageCase() {
        return "O(nk)";
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
        
        if (array[0] instanceof Integer) {
            sortIntegers((Integer[]) array);
        } else if (array[0] instanceof Long) {
            sortLongs((Long[]) array);
        } else if (array[0] instanceof Double) {
            sortWithComparator(array, comparator); // Double is tricky for Radix, fallback
        } else if (array[0] instanceof Number) {
            sortLongsForNumbers(array);
        } else {
            insertionSort(array, comparator);
        }
    }

    private <T> void sortWithComparator(T[] array, Comparator<T> comparator) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        // Find max number of digits using efficient division method
        int maxDigits = 0;
        for (T item : array) {
            long val = 0;
            if (item instanceof Number) {
                val = Math.abs(((Number) item).longValue());
            } else {
                // Fallback: use string length for non-numbers
                val = String.valueOf(item).length();
            }
            // Efficient digit counting without log10
            int digits = countDigits(val);
            if (digits > maxDigits) maxDigits = digits;
        }
        
        long place = 1;
        for (int i = 0; i < maxDigits; i++) {
            countingSortGeneric(array, (int) place, comparator);
            place *= 10;
        }
    }
    
    /**
     * Count digits efficiently without using Math.log10.
     * Uses division method which is faster for integers.
     */
    private int countDigits(long value) {
        if (value == 0) return 1;
        int count = 0;
        while (value > 0) {
            value /= 10;
            count++;
        }
        return count;
    }

    private <T> void sortLongsForNumbers(T[] array) {
        // If they are numbers, we use the generic radix sort which handles Numbers
        sortWithComparator(array, null);
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

    private <T> void countingSortGeneric(T[] array, int place, Comparator<T> comparator) {
        int n = array.length;
        int max = 10;
        
        @SuppressWarnings("unchecked")
        T[] output = (T[]) new Object[n];
        int[] count = new int[max];
        
        for (int i = 0; i < n; i++) {
            int digit = getDigit(array[i], place);
            count[digit]++;
        }
        
        for (int i = 1; i < max; i++) {
            count[i] += count[i - 1];
        }
        
        for (int i = n - 1; i >= 0; i--) {
            int digit = getDigit(array[i], place);
            output[count[digit] - 1] = array[i];
            count[digit]--;
        }
        
        System.arraycopy(output, 0, array, 0, n);
    }

    private <T> int getDigit(T value, int place) {
        if (value instanceof Number) {
            long num = ((Number) value).longValue();
            return (int) ((Math.abs(num) / place) % 10);
        }
        
        // Fallback for non-numbers (already handled in sort() but just in case)
        try {
            long num = Long.parseLong(String.valueOf(value).replaceAll("[^0-9-]", ""));
            return (int) ((Math.abs(num) / place) % 10);
        } catch (Exception e) {
            return 0;
        }
    }

    private void countingSort(Integer[] array, int place) {
        int n = array.length;
        int max = 10;
        
        int[] output = new int[n];
        int[] count = new int[max];
        
        for (int i = 0; i < n; i++) {
            int digit = (array[i] / place) % 10;
            count[digit]++;
        }
        
        for (int i = 1; i < max; i++) {
            count[i] += count[i - 1];
        }
        
        for (int i = n - 1; i >= 0; i--) {
            int digit = (array[i] / place) % 10;
            output[count[digit] - 1] = array[i];
            count[digit]--;
        }
        
        System.arraycopy(output, 0, array, 0, n);
    }

    private void sortIntegers(Integer[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        int max = 0;
        for (int num : array) {
            if (num < 0) {
                max = Math.max(max, -num);
            } else {
                max = Math.max(max, num);
            }
        }
        
        for (int place = 1; max / place > 0; place *= 10) {
            countingSort(array, place);
        }
    }

    private void sortLongs(Long[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        long max = 0;
        for (long num : array) {
            max = Math.max(max, Math.abs(num));
        }
        
        for (long place = 1; max / place > 0; place *= 10) {
            countingSortLongs(array, place);
        }
    }

    private void countingSortLongs(Long[] array, long place) {
        int n = array.length;
        int max = 10;
        
        Long[] output = new Long[n];
        int[] count = new int[max];
        
        for (int i = 0; i < n; i++) {
            int digit = (int) ((array[i] / place) % 10);
            count[digit]++;
        }
        
        for (int i = 1; i < max; i++) {
            count[i] += count[i - 1];
        }
        
        for (int i = n - 1; i >= 0; i--) {
            int digit = (int) ((array[i] / place) % 10);
            output[count[digit] - 1] = array[i];
            count[digit]--;
        }
        
        System.arraycopy(output, 0, array, 0, n);
    }

    private void sortDoubles(Double[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        Double max = 0.0;
        for (Double num : array) {
            if (num != null) {
                max = Math.max(max, Math.abs(num));
            }
        }
        
        if (max == 0.0) {
            return;
        }
        
        for (double place = 1; max / place > 0; place *= 10) {
            countingSortDoubles(array, place);
        }
    }

    private void countingSortDoubles(Double[] array, double place) {
        int n = array.length;
        int max = 10;
        
        Double[] output = new Double[n];
        int[] count = new int[max];
        
        for (int i = 0; i < n; i++) {
            if (array[i] == null) {
                continue;
            }
            // Convert double to long with scaling to avoid precision issues
            long scaledValue = (long) (array[i] * 1000000); // 6 decimal places
            int digit = (int) ((scaledValue / (long) (place * 1000000)) % 10);
            count[digit]++;
        }
        
        for (int i = 1; i < max; i++) {
            count[i] += count[i - 1];
        }
        
        for (int i = n - 1; i >= 0; i--) {
            if (array[i] == null) {
                continue;
            }
            long scaledValue = (long) (array[i] * 1000000);
            int digit = (int) ((scaledValue / (long) (place * 1000000)) % 10);
            output[count[digit] - 1] = array[i];
            count[digit]--;
        }
        
        System.arraycopy(output, 0, array, 0, n);
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
