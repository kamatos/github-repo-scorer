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
}
