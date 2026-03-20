package com.analisis.algoritmos.algorithms.similarity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PearsonCorrelation {

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

        int n = seriesA.size();

        double meanA = calculateMean(seriesA);
        double meanB = calculateMean(seriesB);

        double covariance = calculateCovariance(seriesA, seriesB, meanA, meanB);
        double stdDevA = calculateStdDev(seriesA, meanA);
        double stdDevB = calculateStdDev(seriesB, meanB);

        if (stdDevA == 0 || stdDevB == 0) {
            return null;
        }

        return covariance / (stdDevA * stdDevB);
    }

    public Double calculateWithDouble(List<Double> seriesA, List<Double> seriesB) {
        if (seriesA == null || seriesB == null) {
            return null;
        }

        if (seriesA.isEmpty() || seriesB.isEmpty()) {
            return null;
        }

        if (seriesA.size() != seriesB.size()) {
            return null;
        }

        double meanA = calculateMeanDouble(seriesA);
        double meanB = calculateMeanDouble(seriesB);

        double covariance = calculateCovarianceDouble(seriesA, seriesB, meanA, meanB);
        double stdDevA = calculateStdDevDouble(seriesA, meanA);
        double stdDevB = calculateStdDevDouble(seriesB, meanB);

        if (stdDevA == 0 || stdDevB == 0) {
            return null;
        }

        return covariance / (stdDevA * stdDevB);
    }

    private double calculateMean(List<BigDecimal> series) {
        double sum = 0.0;
        for (BigDecimal value : series) {
            sum += value.doubleValue();
        }
        return sum / series.size();
    }

    private double calculateMeanDouble(List<Double> series) {
        double sum = 0.0;
        for (Double value : series) {
            sum += value;
        }
        return sum / series.size();
    }

    private double calculateCovariance(List<BigDecimal> seriesA, List<BigDecimal> seriesB, 
                                       double meanA, double meanB) {
        double sum = 0.0;
        int n = seriesA.size();

        for (int i = 0; i < n; i++) {
            double diffA = seriesA.get(i).doubleValue() - meanA;
            double diffB = seriesB.get(i).doubleValue() - meanB;
            sum += diffA * diffB;
        }

        return sum / n;
    }

    private double calculateCovarianceDouble(List<Double> seriesA, List<Double> seriesB,
                                            double meanA, double meanB) {
        double sum = 0.0;
        int n = seriesA.size();

        for (int i = 0; i < n; i++) {
            double diffA = seriesA.get(i) - meanA;
            double diffB = seriesB.get(i) - meanB;
            sum += diffA * diffB;
        }

        return sum / n;
    }

    private double calculateStdDev(List<BigDecimal> series, double mean) {
        double sumSquaredDiff = 0.0;
        int n = series.size();

        for (BigDecimal value : series) {
            double diff = value.doubleValue() - mean;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / n);
    }

    private double calculateStdDevDouble(List<Double> series, double mean) {
        double sumSquaredDiff = 0.0;
        int n = series.size();

        for (Double value : series) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / n);
    }

    public String interpretCorrelation(Double correlation) {
        if (correlation == null) {
            return "No se puede calcular";
        }

        double absCorr = Math.abs(correlation);

        if (absCorr >= 0.9) {
            return "Correlación muy fuerte";
        } else if (absCorr >= 0.7) {
            return "Correlación fuerte";
        } else if (absCorr >= 0.5) {
            return "Correlación moderada";
        } else if (absCorr >= 0.3) {
            return "Correlación débil";
        } else {
            return "Correlación muy débil o nula";
        }
    }
}
