package com.gerard.githubreposcorer.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class MathUtilsTest {

    @Test
    @DisplayName("Should calculate freshness score of 1.0 for recently updated repository")
    void shouldCalculateFreshnessScoreOfOneForRecentlyUpdatedRepository() {
        // When
        var result = MathUtils.freshnessFromDays(0, 90);

        // Then
        assertThat(result).isCloseTo(BigDecimal.ONE, offset(BigDecimal.valueOf(0.0001)));
    }

    @Test
    @DisplayName("Should calculate freshness score of approximately 0.5 at half-life")
    void shouldCalculateFreshnessScoreOfApproximatelyHalfAtHalfLife() {
        // When
        var result = MathUtils.freshnessFromDays(90, 90);

        // Then
        assertThat(result).isCloseTo(BigDecimal.valueOf(0.5), Offset.offset(BigDecimal.valueOf(0.01)));
    }

    @Test
    @DisplayName("Should calculate low freshness score for very old repository")
    void shouldCalculateLowFreshnessScoreForVeryOldRepository() {
        // When
        var result = MathUtils.freshnessFromDays(365, 90);

        // Then
        assertThat(result).isLessThan(BigDecimal.valueOf(0.1));
    }

    @Test
    @DisplayName("Should handle negative days since update")
    void shouldHandleNegativeDaysSinceUpdate() {
        // When
        var result = MathUtils.freshnessFromDays(-10, 90);

        // Then
        assertThat(result).isCloseTo(BigDecimal.ONE, offset(BigDecimal.valueOf(0.0001)));
    }

    @Test
    @DisplayName("Should handle zero half-life days")
    void shouldHandleZeroHalfLifeDays() {
        // When
        var result = MathUtils.freshnessFromDays(10, 0);

        // Then
        // With zero half-life, it should return a very small value, not 1
        assertThat(result).isLessThan(BigDecimal.valueOf(0.1));
    }

    @Test
    @DisplayName("Should calculate correct freshness for different half-life values")
    void shouldCalculateCorrectFreshnessForDifferentHalfLifeValues() {
        // When - 30 days with 60 day half-life should be 0.707 (sqrt(0.5))
        var result1 = MathUtils.freshnessFromDays(30, 60);
        
        // When - 60 days with 30 day half-life should be 0.25
        var result2 = MathUtils.freshnessFromDays(60, 30);

        // Then
        assertThat(result1).isCloseTo(BigDecimal.valueOf(0.707), offset(BigDecimal.valueOf(0.01)));
        assertThat(result2).isCloseTo(BigDecimal.valueOf(0.25), offset(BigDecimal.valueOf(0.01)));
    }
}
