package com.analisis.algoritmos.algorithms.similarity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CosineSimilarity {

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

        double dotProduct = calculateDotProduct(seriesA, seriesB);
        double magnitudeA = calculateMagnitude(seriesA);
        double magnitudeB = calculateMagnitude(seriesB);

        if (magnitudeA == 0 || magnitudeB == 0) {
            return null;
        }

        return dotProduct / (magnitudeA * magnitudeB);
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

        double dotProduct = calculateDotProductDouble(seriesA, seriesB);
        double magnitudeA = calculateMagnitudeDouble(seriesA);
        double magnitudeB = calculateMagnitudeDouble(seriesB);

        if (magnitudeA == 0 || magnitudeB == 0) {
            return null;
        }

        return dotProduct / (magnitudeA * magnitudeB);
    }

    public Double calculateAngularDistance(List<BigDecimal> seriesA, List<BigDecimal> seriesB) {
        Double similarity = calculate(seriesA, seriesB);
        if (similarity == null) {
            return null;
        }
        return Math.acos(similarity) / Math.PI;
    }

    private double calculateDotProduct(List<BigDecimal> seriesA, List<BigDecimal> seriesB) {
        double sum = 0.0;
        for (int i = 0; i < seriesA.size(); i++) {
            sum += seriesA.get(i).doubleValue() * seriesB.get(i).doubleValue();
        }
        return sum;
    }

    private double calculateDotProductDouble(List<Double> seriesA, List<Double> seriesB) {
        double sum = 0.0;
        for (int i = 0; i < seriesA.size(); i++) {
            sum += seriesA.get(i) * seriesB.get(i);
        }
        return sum;
    }

    private double calculateMagnitude(List<BigDecimal> series) {
        double sumSquares = 0.0;
        for (BigDecimal value : series) {
            double v = value.doubleValue();
            sumSquares += v * v;
        }
        return Math.sqrt(sumSquares);
    }

    private double calculateMagnitudeDouble(List<Double> series) {
        double sumSquares = 0.0;
        for (Double value : series) {
            sumSquares += value * value;
        }
        return Math.sqrt(sumSquares);
    }

    public String interpretSimilarity(Double similarity) {
        if (similarity == null) {
            return "No se puede calcular";
        }

        if (similarity >= 0.9) {
            return "Similitud muy alta";
        } else if (similarity >= 0.7) {
            return "Similitud alta";
        } else if (similarity >= 0.5) {
            return "Similitud moderada";
        } else if (similarity >= 0.3) {
            return "Similitud baja";
        } else if (similarity >= 0) {
            return "Similitud muy baja";
        } else {
            return "Similitud negativa (series opuestas)";
        }
    }
}
