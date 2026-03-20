package com.analisis.algoritmos.algorithms.preprocessing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleMovingAverage {

    public List<Double> calculate(List<BigDecimal> prices, int period) {
        if (prices == null || prices.size() < period || period <= 0) {
            return new ArrayList<>();
        }

        List<Double> sma = new ArrayList<>();

        for (int i = 0; i < prices.size(); i++) {
            if (i < period - 1) {
                sma.add(null);
            } else {
                double sum = 0.0;
                for (int j = 0; j < period; j++) {
                    sum += prices.get(i - j).doubleValue();
                }
                sma.add(sum / period);
            }
        }

        return sma;
    }

    public List<Double> calculateWithDouble(List<Double> prices, int period) {
        if (prices == null || prices.size() < period || period <= 0) {
            return new ArrayList<>();
        }

        List<Double> sma = new ArrayList<>();

        for (int i = 0; i < prices.size(); i++) {
            if (i < period - 1) {
                sma.add(null);
            } else {
                double sum = 0.0;
                for (int j = 0; j < period; j++) {
                    sum += prices.get(i - j);
                }
                sma.add(sum / period);
            }
        }

        return sma;
    }

    public Double calculateLatest(List<BigDecimal> prices, int period) {
        if (prices == null || prices.size() < period || period <= 0) {
            return null;
        }

        double sum = 0.0;
        int startIndex = prices.size() - period;
        
        for (int i = startIndex; i < prices.size(); i++) {
            sum += prices.get(i).doubleValue();
        }
        
        return sum / period;
    }

    public Double calculateExponential(List<BigDecimal> prices, int period) {
        if (prices == null || prices.size() < period || period <= 0) {
            return null;
        }

        double multiplier = 2.0 / (period + 1);
        List<Double> ema = new ArrayList<>();
        
        double sum = 0.0;
        for (int i = 0; i < period; i++) {
            sum += prices.get(i).doubleValue();
        }
        ema.add(sum / period);
        
        for (int i = period; i < prices.size(); i++) {
            double currentPrice = prices.get(i).doubleValue();
            double previousEma = ema.get(ema.size() - 1);
            double emaValue = (currentPrice * multiplier) + (previousEma * (1 - multiplier));
            ema.add(emaValue);
        }
        
        return ema.get(ema.size() - 1);
    }
}
