package com.gerard.githubreposcorer.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MathUtils {

    public static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * Normalized logarithmic function using BigDecimal for precise calculations
     *
     * @param x   The input value
     * @param cap The cap value
     * @return The normalized logarithmic result
     */
    public static BigDecimal normLog(int x, int cap) {
        if (cap <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal logX = BigDecimal.valueOf(Math.log1p(x));
        BigDecimal logCap = BigDecimal.valueOf(Math.log1p(cap));

        // Return log1p(x) / log1p(cap)
        return logX.divide(logCap, MATH_CONTEXT);
    }

    /**
     * Calculate freshness score based on days since update using exponential decay
     *
     * @param daysSinceUpdate The number of days since the last update
     * @param halfLifeDays    The half-life in days for the decay calculation
     * @return The freshness score (0.0 to 1.0)
     */
    public static BigDecimal freshnessFromDays(int daysSinceUpdate, int halfLifeDays) {
        int d = Math.max(0, daysSinceUpdate);
        
        // Calculate lambda using BigDecimal operations
        BigDecimal log2 = BigDecimal.valueOf(Math.log(2.0));
        BigDecimal maxHalfLife = BigDecimal.valueOf(Math.max(1, halfLifeDays));
        BigDecimal lambda = log2.divide(maxHalfLife, MATH_CONTEXT);
        
        // Calculate -lambda * d using BigDecimal operations
        BigDecimal negativeLambda = lambda.negate();
        BigDecimal days = BigDecimal.valueOf(d);
        BigDecimal exponent = negativeLambda.multiply(days, MATH_CONTEXT);
        
        // Convert to double for Math.exp, then back to BigDecimal
        double expResult = Math.exp(exponent.doubleValue());
        return BigDecimal.valueOf(expResult).setScale(MATH_CONTEXT.getPrecision(), MATH_CONTEXT.getRoundingMode());
    }
    
}
