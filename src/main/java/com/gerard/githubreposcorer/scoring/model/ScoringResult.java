package com.gerard.githubreposcorer.scoring.model;

import java.math.BigDecimal;

public record ScoringResult(
        String ruleName,
        BigDecimal score,
        BigDecimal weight,
        boolean success,
        String errorMessage
) {

    public static ScoringResult success(String ruleName, BigDecimal score, BigDecimal weight) {
        return new ScoringResult(ruleName, score, weight, true, null);
    }

    public static ScoringResult failure(String ruleName, String errorMessage) {
        return new ScoringResult(ruleName, BigDecimal.ZERO, BigDecimal.ZERO, false, errorMessage);
    }
}
