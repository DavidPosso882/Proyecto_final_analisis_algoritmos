package com.analisis.algoritmos.algorithms.similarity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Component
public class EuclideanDistance {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    public Double calculate(List<BigDecimal> seriesA, List<BigDecimal> seriesB) {
        if (seriesA == null || seriesB == null) {
            return null;
        }

        if (seriesA.isEmpty() || seriesB.isEmpty()) {
            return null;
        }

        if (seriesA.size() != seriesB.size()) {
            return null;
        }

        double sumSquaredDifferences = 0.0;

        for (int i = 0; i < seriesA.size(); i++) {
            double diff = seriesA.get(i).doubleValue() - seriesB.get(i).doubleValue();
            sumSquaredDifferences += diff * diff;
        }

        return Math.sqrt(sumSquaredDifferences);
    }

    public Double calculateOnReturns(List<Double> returnsA, List<Double> returnsB) {
        if (returnsA == null || returnsB == null) {
            return null;
        }

        if (returnsA.isEmpty() || returnsB.isEmpty()) {
            return null;
        }

        if (returnsA.size() != returnsB.size()) {
            return null;
        }

        double sumSquaredDifferences = 0.0;

        for (int i = 0; i < returnsA.size(); i++) {
            double diff = returnsA.get(i) - returnsB.get(i);
            sumSquaredDifferences += diff * diff;
        }

        return Math.sqrt(sumSquaredDifferences);
    }

    public Double calculateNormalized(List<BigDecimal> seriesA, List<BigDecimal> seriesB) {
        Double distance = calculate(seriesA, seriesB);

        if (distance == null) {
            return null;
        }

        double n = seriesA.size();
        return distance / Math.sqrt(n);
    }

    public Double calculateWithScaling(List<BigDecimal> seriesA, List<BigDecimal> seriesB) {
        if (seriesA == null || seriesB == null || seriesA.isEmpty() || seriesB.isEmpty()) {
            return null;
        }

        double minLen = Math.min(seriesA.size(), seriesB.size());

        double sumSquaredDifferences = 0.0;
        for (int i = 0; i < minLen; i++) {
            double diff = seriesA.get(i).doubleValue() - seriesB.get(i).doubleValue();
            sumSquaredDifferences += diff * diff;
        }

        return Math.sqrt(sumSquaredDifferences) / Math.sqrt(minLen);
    }
}
