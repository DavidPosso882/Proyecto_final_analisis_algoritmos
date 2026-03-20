package com.analisis.algoritmos.algorithms.patterns;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class VolatilityCalculator {

    private static final int TRADING_DAYS_PER_YEAR = 252;

    public Double calculateVolatility(List<Double> returns) {
        if (returns == null || returns.size() < 2) {
            return null;
        }

        Double mean = calculateMean(returns);
        if (mean == null) {
            return null;
        }

        double sumSquaredDifferences = 0.0;
        int count = 0;

        for (Double ret : returns) {
            if (ret != null) {
                double diff = ret - mean;
                sumSquaredDifferences += diff * diff;
                count++;
            }
        }

        if (count < 2) {
            return null;
        }

        return Math.sqrt(sumSquaredDifferences / (count - 1));
    }

    public Double calculateVolatilityFromPrices(List<BigDecimal> prices) {
        if (prices == null || prices.size() < 2) {
            return null;
        }

        List<Double> returns = new java.util.ArrayList<>();
        returns.add(null);

        for (int i = 1; i < prices.size(); i++) {
            double currentPrice = prices.get(i).doubleValue();
            double previousPrice = prices.get(i - 1).doubleValue();

            if (previousPrice > 0 && currentPrice > 0) {
                double logReturn = Math.log(currentPrice / previousPrice);
                returns.add(logReturn);
            } else {
                returns.add(null);
            }
        }

        returns.remove(0);

        return calculateVolatility(returns);
    }

    public Double calculateAnnualizedVolatility(List<Double> returns) {
        Double dailyVolatility = calculateVolatility(returns);
        
        if (dailyVolatility == null) {
            return null;
        }

        return dailyVolatility * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    public Double calculateAnnualizedVolatilityFromPrices(List<BigDecimal> prices) {
        Double dailyVolatility = calculateVolatilityFromPrices(prices);
        
        if (dailyVolatility == null) {
            return null;
        }

        return dailyVolatility * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    public Double calculateRollingVolatility(List<Double> returns, int windowSize) {
        if (returns == null || returns.size() < windowSize) {
            return null;
        }

        int startIndex = returns.size() - windowSize;
        List<Double> windowReturns = returns.subList(startIndex, returns.size());

        return calculateVolatility(windowReturns);
    }

    private Double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        double sum = 0.0;
        int count = 0;

        for (Double value : values) {
            if (value != null) {
                sum += value;
                count++;
            }
        }

        return count > 0 ? sum / count : null;
    }

    public String classifyVolatility(Double annualizedVolatility) {
        if (annualizedVolatility == null) {
            return "No clasificable";
        }

        double volPercent = annualizedVolatility * 100;

        if (volPercent < 10) {
            return "Muy baja";
        } else if (volPercent < 20) {
            return "Baja";
        } else if (volPercent < 30) {
            return "Moderada";
        } else if (volPercent < 50) {
            return "Alta";
        } else {
            return "Muy alta";
        }
    }

    public Double calculateVariance(List<Double> returns) {
        Double volatility = calculateVolatility(returns);
        
        return volatility != null ? volatility * volatility : null;
    }

    public Double calculateDownsideDeviation(List<Double> returns, double targetReturn) {
        if (returns == null || returns.isEmpty()) {
            return null;
        }

        double sumSquaredNegative = 0.0;
        int count = 0;

        for (Double ret : returns) {
            if (ret != null) {
                double downside = ret - targetReturn;
                if (downside < 0) {
                    sumSquaredNegative += downside * downside;
                    count++;
                }
            }
        }

        if (count < 2) {
            return null;
        }

        return Math.sqrt(sumSquaredNegative / count);
    }
}
