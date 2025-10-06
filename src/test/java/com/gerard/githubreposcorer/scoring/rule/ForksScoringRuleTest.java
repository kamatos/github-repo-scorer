package com.gerard.githubreposcorer.scoring.rule;

import com.gerard.githubreposcorer.config.ScoringProperties;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ForksScoringRuleTest {

    @Test
    @DisplayName("Should calculate high forks score for repository with many forks")
    void shouldCalculateHighForksScoreForRepositoryWithManyForks() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(10000);
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(8000)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        assertThat(result.score()).isGreaterThan(BigDecimal.valueOf(0.8));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should calculate low forks score for repository with few forks")
    void shouldCalculateLowForksScoreForRepositoryWithFewForks() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(10000);
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(10)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        // With 10 forks out of 10000 cap, should be around 0.26
        assertThat(result.score()).isCloseTo(BigDecimal.valueOf(0.26), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should calculate medium forks score for repository at cap")
    void shouldCalculateMediumForksScoreForRepositoryAtCap() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(10000);
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(10000)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        // At exactly the cap, the score should be 1.0
        assertThat(result.score()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.0001)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should handle zero forks")
    void shouldHandleZeroForks() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(10000);
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(0)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        // With 0 forks, the score should be 0
        assertThat(result.score()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.0001)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should handle forks above cap")
    void shouldHandleForksAboveCap() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(10000);
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(50000) // Above cap
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        // Above cap, the score should be greater than 1.0
        assertThat(result.score()).isGreaterThan(BigDecimal.ONE);
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should handle different cap values")
    void shouldHandleDifferentCapValues() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(1000); // Lower cap
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(500)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        // With 500 forks out of 1000 cap, should be around 0.9
        assertThat(result.score()).isCloseTo(BigDecimal.valueOf(0.9), Offset.offset(BigDecimal.valueOf(0.1)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should handle zero cap")
    void shouldHandleZeroCap() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(0); // Zero cap
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(1000)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        // With zero cap, the score should be 0
        assertThat(result.score()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.0001)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should return correct weight")
    void shouldReturnCorrectWeight() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);

        // When
        var weight = rule.getWeight();

        // Then
        assertThat(weight).isEqualTo(BigDecimal.valueOf(0.2));
    }

    @Test
    @DisplayName("Should return correct name")
    void shouldReturnCorrectName() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        var rule = new ForksScoringRule(forksConfig);

        // When
        var name = rule.getName();

        // Then
        assertThat(name).isEqualTo("ForksScoringRule");
    }

    @Test
    @DisplayName("Should handle negative forks with error")
    void shouldHandleNegativeForksWithError() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(10000);
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(-100) // Negative forks
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isFalse();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        assertThat(result.errorMessage()).isNotNull();
    }

    @Test
    @DisplayName("Should calculate score for typical repository values")
    void shouldCalculateScoreForTypicalRepositoryValues() {
        // Given
        var forksConfig = new ScoringProperties.Forks();
        forksConfig.setCap(10000);
        forksConfig.setWeight(0.2);

        var rule = new ForksScoringRule(forksConfig);
        var context = ScoringContext.builder()
                .forks(2500) // Typical value
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("ForksScoringRule");
        // With 2500 forks out of 10000 cap, should be around 0.85
        assertThat(result.score()).isCloseTo(BigDecimal.valueOf(0.85), Offset.offset(BigDecimal.valueOf(0.05)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.2));
    }
}
