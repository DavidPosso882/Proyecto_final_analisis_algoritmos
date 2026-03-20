package com.analisis.algoritmos.algorithms.preprocessing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReturnsCalculator {

    public List<Double> calculateLogReturns(List<BigDecimal> prices) {
        if (prices == null || prices.size() < 2) {
            return new ArrayList<>();
        }

        List<Double> returns = new ArrayList<>();
        returns.add(null);

        for (int i = 1; i < prices.size(); i++) {
            double currentPrice = prices.get(i).doubleValue();
            double previousPrice = prices.get(i - 1).doubleValue();

            if (previousPrice <= 0 || currentPrice <= 0) {
                returns.add(null);
            } else {
                double logReturn = Math.log(currentPrice / previousPrice);
                returns.add(logReturn);
            }
        }

        return returns;
    }

    public List<Double> calculateSimpleReturns(List<BigDecimal> prices) {
        if (prices == null || prices.size() < 2) {
            return new ArrayList<>();
        }

        List<Double> returns = new ArrayList<>();
        returns.add(null);

        for (int i = 1; i < prices.size(); i++) {
            double currentPrice = prices.get(i).doubleValue();
            double previousPrice = prices.get(i - 1).doubleValue();

            if (previousPrice <= 0) {
                returns.add(null);
            } else {
                double simpleReturn = (currentPrice - previousPrice) / previousPrice;
                returns.add(simpleReturn);
            }
        }

        return returns;
    }

    public List<Double> calculatePercentageReturns(List<BigDecimal> prices) {
        List<Double> simpleReturns = calculateSimpleReturns(prices);
        
        List<Double> percentageReturns = new ArrayList<>();
        for (Double ret : simpleReturns) {
            if (ret == null) {
                percentageReturns.add(null);
            } else {
                percentageReturns.add(ret * 100);
            }
        }
        
        return percentageReturns;
    }

    public Double calculateCumulativeReturn(List<Double> returns) {
        if (returns == null || returns.isEmpty()) {
            return null;
        }

        double cumulative = 0.0;
        for (Double ret : returns) {
            if (ret != null) {
                cumulative += ret;
            }
        }

        return cumulative;
    }

    public Double calculateAverageReturn(List<Double> returns) {
        if (returns == null || returns.isEmpty()) {
            return null;
        }

        double sum = 0.0;
        int count = 0;

        for (Double ret : returns) {
            if (ret != null) {
                sum += ret;
                count++;
            }
        }

        return count > 0 ? sum / count : null;
    }

    public Double calculateAnnualizedReturn(List<Double> returns, int periodsPerYear) {
        Double avgReturn = calculateAverageReturn(returns);
        
        if (avgReturn == null) {
            return null;
        }

        return avgReturn * periodsPerYear;
    }
}
