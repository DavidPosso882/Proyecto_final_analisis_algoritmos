package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class BucketSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "BucketSort";
    }

    @Override
    public String getComplexity() {
        return "O(n + k)";
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
        
        // If not a number, we can't easily determine ranges for buckets
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
        
        if (min.equals(max)) {
            return;
        }
        
        int bucketCount = Math.min(n, 1000); // Limit number of buckets to avoid memory issues
        List<T>[] buckets = new ArrayList[bucketCount];
        
        for (int i = 0; i < bucketCount; i++) {
            buckets[i] = new ArrayList<>();
        }
        
        double minVal = ((Number) min).doubleValue();
        double maxVal = ((Number) max).doubleValue();
        double range = maxVal - minVal;
        
        if (range == 0) {
            return;
        }

        for (T value : array) {
            double currentVal = ((Number) value).doubleValue();
            int bucketIndex = (int) (((currentVal - minVal) / range) * (bucketCount - 1));
            // Safety check for index bounds
            if (bucketIndex < 0) bucketIndex = 0;
            if (bucketIndex >= bucketCount) bucketIndex = bucketCount - 1;
            buckets[bucketIndex].add(value);
        }
        
        // Sort each bucket efficiently
        for (List<T> bucket : buckets) {
            if (!bucket.isEmpty()) {
                // For numeric types, use counting sort within bucket for O(n) complexity
                Object first = bucket.get(0);
                if (first instanceof Integer) {
                    // Safe cast: we know all elements in bucket are same type
                    @SuppressWarnings("unchecked")
                    List<Integer> intBucket = (List<Integer>) (List<?>) bucket;
                    sortIntegerBucket(intBucket);
                } else if (first instanceof Long) {
                    @SuppressWarnings("unchecked")
                    List<Long> longBucket = (List<Long>) (List<?>) bucket;
                    sortLongBucket(longBucket);
                } else if (first instanceof Double) {
                    @SuppressWarnings("unchecked")
                    List<Double> doubleBucket = (List<Double>) (List<?>) bucket;
                    sortDoubleBucket(doubleBucket);
                } else {
                    // For non-numeric types, use insertion sort (efficient for small buckets)
                    sortGenericBucket(bucket, comparator);
                }
            }
        }
        
        int index = 0;
        for (List<T> bucket : buckets) {
            for (T value : bucket) {
                array[index++] = value;
            }
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

    private void sortIntegerBucket(List<Integer> bucket) {
        if (bucket.size() <= 1) return;
        
        // Find min and max in bucket
        int min = bucket.get(0);
        int max = bucket.get(0);
        for (int val : bucket) {
            if (val < min) min = val;
            if (val > max) max = val;
        }
        
        int range = max - min + 1;
        if (range > bucket.size() * 10) {
            // Range too large for counting sort, use insertion sort
            sortGenericBucketInt(bucket);
            return;
        }
        
        // Counting sort
        int[] count = new int[range];
        for (int val : bucket) {
            count[val - min]++;
        }
        
        // Reconstruct sorted list
        bucket.clear();
        for (int i = 0; i < range; i++) {
            for (int j = 0; j < count[i]; j++) {
                bucket.add(min + i);
            }
        }
    }

    private void sortLongBucket(List<Long> bucket) {
        if (bucket.size() <= 1) return;
        
        // For long, use insertion sort as counting sort may be inefficient
        sortGenericBucketLong(bucket);
    }

    private void sortDoubleBucket(List<Double> bucket) {
        if (bucket.size() <= 1) return;
        
        // For double, use insertion sort
        sortGenericBucketDouble(bucket);
    }

    private <T> void sortGenericBucket(List<T> bucket, Comparator<T> comparator) {
        // Convert to array and use insertion sort
        @SuppressWarnings("unchecked")
        T[] array = (T[]) new Object[bucket.size()];
        bucket.toArray(array);
        
        insertionSort(array, comparator);
        
        // Update bucket
        bucket.clear();
        for (T item : array) {
            bucket.add(item);
        }
    }

    private void sortGenericBucketInt(List<Integer> bucket) {
        // Insertion sort for integers
        for (int i = 1; i < bucket.size(); i++) {
            int key = bucket.get(i);
            int j = i - 1;
            while (j >= 0 && bucket.get(j) > key) {
                bucket.set(j + 1, bucket.get(j));
                j--;
            }
            bucket.set(j + 1, key);
        }
    }

    private void sortGenericBucketLong(List<Long> bucket) {
        // Insertion sort for longs
        for (int i = 1; i < bucket.size(); i++) {
            long key = bucket.get(i);
            int j = i - 1;
            while (j >= 0 && bucket.get(j) > key) {
                bucket.set(j + 1, bucket.get(j));
                j--;
            }
            bucket.set(j + 1, key);
        }
    }

    private void sortGenericBucketDouble(List<Double> bucket) {
        // Insertion sort for doubles
        for (int i = 1; i < bucket.size(); i++) {
            double key = bucket.get(i);
            int j = i - 1;
            while (j >= 0 && bucket.get(j) > key) {
                bucket.set(j + 1, bucket.get(j));
                j--;
            }
            bucket.set(j + 1, key);
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
