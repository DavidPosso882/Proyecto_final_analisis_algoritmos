package com.analisis.algoritmos.algorithms.similarity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DynamicTimeWarping {

    public Double calculate(List<BigDecimal> seriesA, List<BigDecimal> seriesB) {
        if (seriesA == null || seriesB == null) {
            return null;
        }

        if (seriesA.isEmpty() || seriesB.isEmpty()) {
            return null;
        }

        int n = seriesA.size();
        int m = seriesB.size();

        // Use lazy initialization: only initialize borders
        double[][] dtw = new double[n + 1][m + 1];

        // Initialize only the first row and column to infinity
        for (int i = 0; i <= n; i++) {
            dtw[i][0] = Double.POSITIVE_INFINITY;
        }
        for (int j = 0; j <= m; j++) {
            dtw[0][j] = Double.POSITIVE_INFINITY;
        }
        dtw[0][0] = 0.0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.abs(
                    seriesA.get(i - 1).doubleValue() - 
                    seriesB.get(j - 1).doubleValue()
                );

                // Use local variables to avoid multiple array accesses
                double up = dtw[i - 1][j];
                double left = dtw[i][j - 1];
                double diag = dtw[i - 1][j - 1];
                
                dtw[i][j] = cost + Math.min(Math.min(up, left), diag);
            }
        }

        return dtw[n][m];
    }

    public Double calculateWithDouble(List<Double> seriesA, List<Double> seriesB) {
        if (seriesA == null || seriesB == null) {
            return null;
        }

        if (seriesA.isEmpty() || seriesB.isEmpty()) {
            return null;
        }

        int n = seriesA.size();
        int m = seriesB.size();

        double[][] dtw = new double[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dtw[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        dtw[0][0] = 0.0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.abs(seriesA.get(i - 1) - seriesB.get(j - 1));

                dtw[i][j] = cost + Math.min(
                    Math.min(dtw[i - 1][j], dtw[i][j - 1]),
                    dtw[i - 1][j - 1]
                );
            }
        }

        return dtw[n][m];
    }

    public Double calculateNormalized(List<BigDecimal> seriesA, List<BigDecimal> seriesB) {
        Double dtwDistance = calculate(seriesA, seriesB);
        if (dtwDistance == null) {
            return null;
        }

        int n = seriesA.size();
        int m = seriesB.size();
        return dtwDistance / Math.sqrt(n + m);
    }

    public Double calculateWithWindow(List<BigDecimal> seriesA, List<BigDecimal> seriesB, int windowSize) {
        if (seriesA == null || seriesB == null || windowSize < 1) {
            return calculate(seriesA, seriesB);
        }

        int n = seriesA.size();
        int m = seriesB.size();

        double[][] dtw = new double[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dtw[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        dtw[0][0] = 0.0;

        for (int i = 1; i <= n; i++) {
            int jStart = Math.max(1, i - windowSize);
            int jEnd = Math.min(m, i + windowSize);

            for (int j = jStart; j <= jEnd; j++) {
                double cost = Math.abs(
                    seriesA.get(i - 1).doubleValue() - 
                    seriesB.get(j - 1).doubleValue()
                );

                dtw[i][j] = cost + Math.min(
                    Math.min(dtw[i - 1][j], dtw[i][j - 1]),
                    dtw[i - 1][j - 1]
                );
            }
        }

        return dtw[n][m];
    }
}
