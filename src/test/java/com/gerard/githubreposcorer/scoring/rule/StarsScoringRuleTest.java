package com.gerard.githubreposcorer.scoring.rule;

import com.gerard.githubreposcorer.config.ScoringProperties;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class StarsScoringRuleTest {

    @Test
    @DisplayName("Should calculate high stars score for repository with many stars")
    void shouldCalculateHighStarsScoreForRepositoryWithManyStars() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(10000);
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(8000)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        assertThat(result.score()).isGreaterThan(BigDecimal.valueOf(0.8));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should calculate low stars score for repository with few stars")
    void shouldCalculateLowStarsScoreForRepositoryWithFewStars() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(10000);
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(10)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        // With 10 stars out of 10000 cap, should be around 0.26
        assertThat(result.score()).isCloseTo(BigDecimal.valueOf(0.26), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should calculate medium stars score for repository at cap")
    void shouldCalculateMediumStarsScoreForRepositoryAtCap() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(10000);
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(10000)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        // At exactly the cap, the score should be 1.0
        assertThat(result.score()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.0001)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should handle zero stars")
    void shouldHandleZeroStars() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(10000);
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(0)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        // With 0 stars, the score should be 0
        assertThat(result.score()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.0001)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should handle stars above cap")
    void shouldHandleStarsAboveCap() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(10000);
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(50000) // Above cap
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        // Above cap, the score should be greater than 1.0
        assertThat(result.score()).isGreaterThan(BigDecimal.ONE);
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should handle different cap values")
    void shouldHandleDifferentCapValues() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(1000); // Lower cap
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(500)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        // With 500 stars out of 1000 cap, should be around 0.9
        assertThat(result.score()).isCloseTo(BigDecimal.valueOf(0.9), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should handle zero cap")
    void shouldHandleZeroCap() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(0); // Zero cap
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(1000)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        // With zero cap, the score should be 0
        assertThat(result.score()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.0001)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should return correct weight")
    void shouldReturnCorrectWeight() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);

        // When
        var weight = rule.getWeight();

        // Then
        assertThat(weight).isEqualTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("Should return correct name")
    void shouldReturnCorrectName() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        var rule = new StarsScoringRule(starsConfig);

        // When
        var name = rule.getName();

        // Then
        assertThat(name).isEqualTo("StarsScoringRule");
    }

    @Test
    @DisplayName("Should handle negative stars with error")
    void shouldHandleNegativeStarsWithError() {
        // Given
        var starsConfig = new ScoringProperties.Stars();
        starsConfig.setCap(10000);
        starsConfig.setWeight(0.45);

        var rule = new StarsScoringRule(starsConfig);
        var context = ScoringContext.builder()
                .stars(-100) // Negative stars
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isFalse();
        assertThat(result.ruleName()).isEqualTo("StarsScoringRule");
        assertThat(result.errorMessage()).isNotNull();
    }
}
