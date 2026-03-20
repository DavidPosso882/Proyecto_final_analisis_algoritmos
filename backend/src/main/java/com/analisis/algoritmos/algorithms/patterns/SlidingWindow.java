package com.analisis.algoritmos.algorithms.patterns;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SlidingWindow {

    public record WindowResult(int startIndex, int endIndex, List<Double> values, double average) {}

    public List<WindowResult> applyWindow(List<BigDecimal> data, int windowSize) {
        if (data == null || data.size() < windowSize || windowSize <= 0) {
            return new ArrayList<>();
        }

        List<WindowResult> results = new ArrayList<>();

        for (int i = 0; i <= data.size() - windowSize; i++) {
            List<Double> windowValues = new ArrayList<>();
            double sum = 0.0;

            for (int j = 0; j < windowSize; j++) {
                double value = data.get(i + j).doubleValue();
                windowValues.add(value);
                sum += value;
            }

            double average = sum / windowSize;
            results.add(new WindowResult(i, i + windowSize - 1, windowValues, average));
        }

        return results;
    }

    public List<Double> calculateMovingAverage(List<BigDecimal> data, int windowSize) {
        if (data == null || data.size() < windowSize || windowSize <= 0) {
            return new ArrayList<>();
        }

        List<Double> movingAverages = new ArrayList<>();

        for (int i = 0; i <= data.size() - windowSize; i++) {
            double sum = 0.0;
            for (int j = 0; j < windowSize; j++) {
                sum += data.get(i + j).doubleValue();
            }
            movingAverages.add(sum / windowSize);
        }

        return movingAverages;
    }

    public int countUpwardPatterns(List<BigDecimal> prices, int windowSize) {
        if (prices == null || prices.size() < windowSize + 1 || windowSize <= 0) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i <= prices.size() - windowSize - 1; i++) {
            boolean allUp = true;

            for (int j = 0; j < windowSize; j++) {
                double current = prices.get(i + j + 1).doubleValue();
                double previous = prices.get(i + j).doubleValue();

                if (current <= previous) {
                    allUp = false;
                    break;
                }
            }

            if (allUp) {
                count++;
            }
        }

        return count;
    }

    public int countDownwardPatterns(List<BigDecimal> prices, int windowSize) {
        if (prices == null || prices.size() < windowSize + 1 || windowSize <= 0) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i <= prices.size() - windowSize - 1; i++) {
            boolean allDown = true;

            for (int j = 0; j < windowSize; j++) {
                double current = prices.get(i + j + 1).doubleValue();
                double previous = prices.get(i + j).doubleValue();

                if (current >= previous) {
                    allDown = false;
                    break;
                }
            }

            if (allDown) {
                count++;
            }
        }

        return count;
    }

    public List<Double> findLocalMaxima(List<BigDecimal> data, int windowSize) {
        if (data == null || data.size() < windowSize || windowSize <= 0) {
            return new ArrayList<>();
        }

        List<Double> maxima = new ArrayList<>();
        int halfWindow = windowSize / 2;

        for (int i = halfWindow; i < data.size() - halfWindow; i++) {
            boolean isMax = true;
            double currentValue = data.get(i).doubleValue();

            for (int j = i - halfWindow; j <= i + halfWindow; j++) {
                if (j != i && data.get(j).doubleValue() >= currentValue) {
                    isMax = false;
                    break;
                }
            }

            if (isMax) {
                maxima.add(currentValue);
            }
        }

        return maxima;
    }

    public List<Double> findLocalMinima(List<BigDecimal> data, int windowSize) {
        if (data == null || data.size() < windowSize || windowSize <= 0) {
            return new ArrayList<>();
        }

        List<Double> minima = new ArrayList<>();
        int halfWindow = windowSize / 2;

        for (int i = halfWindow; i < data.size() - halfWindow; i++) {
            boolean isMin = true;
            double currentValue = data.get(i).doubleValue();

            for (int j = i - halfWindow; j <= i + halfWindow; j++) {
                if (j != i && data.get(j).doubleValue() <= currentValue) {
                    isMin = false;
                    break;
                }
            }

            if (isMin) {
                minima.add(currentValue);
            }
        }

        return minima;
    }
}
