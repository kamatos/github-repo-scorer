package com.gerard.githubreposcorer.scoring.rule;

import com.gerard.githubreposcorer.config.ScoringProperties;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import com.gerard.githubreposcorer.scoring.model.ScoringResult;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FreshnessScoringRuleTest {

    @Test
    @DisplayName("Should calculate high freshness score for recently updated repository")
    void shouldCalculateHighFreshnessScoreForRecentlyUpdatedRepository() {
        // Given
        var freshnessConfig = new ScoringProperties.Freshness();
        freshnessConfig.setHalfLifeDays(90);
        freshnessConfig.setWeight(0.35);

        var rule = new FreshnessScoringRule(freshnessConfig);
        var context = ScoringContext.builder()
                .daysSinceUpdate(1)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("FreshnessScoringRule");
        assertThat(result.score()).isGreaterThan(BigDecimal.valueOf(0.9));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.35));
    }

    @Test
    @DisplayName("Should calculate low freshness score for old repository")
    void shouldCalculateLowFreshnessScoreForOldRepository() {
        // Given
        var freshnessConfig = new ScoringProperties.Freshness();
        freshnessConfig.setHalfLifeDays(90);
        freshnessConfig.setWeight(0.35);

        var rule = new FreshnessScoringRule(freshnessConfig);
        var context = ScoringContext.builder()
                .daysSinceUpdate(365)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("FreshnessScoringRule");
        assertThat(result.score()).isLessThan(BigDecimal.valueOf(0.1));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.35));
    }

    @Test
    @DisplayName("Should calculate medium freshness score for repository updated at half-life")
    void shouldCalculateMediumFreshnessScoreForRepositoryUpdatedAtHalfLife() {
        // Given
        var freshnessConfig = new ScoringProperties.Freshness();
        freshnessConfig.setHalfLifeDays(90);
        freshnessConfig.setWeight(0.35);

        var rule = new FreshnessScoringRule(freshnessConfig);
        var context = ScoringContext.builder()
                .daysSinceUpdate(90)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("FreshnessScoringRule");
        // At exactly half-life, the score should be approximately 0.5
        assertThat(result.score()).isCloseTo(BigDecimal.valueOf(0.5), Offset.offset(BigDecimal.valueOf(0.01)));
        assertThat(result.weight()).isEqualTo(BigDecimal.valueOf(0.35));
    }

    @Test
    @DisplayName("Should handle zero days since update")
    void shouldHandleZeroDaysSinceUpdate() {
        // Given
        var freshnessConfig = new ScoringProperties.Freshness();
        freshnessConfig.setHalfLifeDays(90);
        freshnessConfig.setWeight(0.35);

        var rule = new FreshnessScoringRule(freshnessConfig);
        var context = ScoringContext.builder()
                .daysSinceUpdate(0)
                .build();

        // When
        rule.execute(context);

        // Then
        var results = context.getResults();
        assertThat(results).hasSize(1);
        
        var result = results.get(0);
        assertThat(result.success()).isTrue();
        assertThat(result.ruleName()).isEqualTo("FreshnessScoringRule");
        assertThat(result.score()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.0001)));
    }

    @Test
    @DisplayName("Should return correct weight")
    void shouldReturnCorrectWeight() {
        // Given
        var freshnessConfig = new ScoringProperties.Freshness();
        freshnessConfig.setWeight(0.35);

        var rule = new FreshnessScoringRule(freshnessConfig);

        // When
        var weight = rule.getWeight();

        // Then
        assertThat(weight).isEqualTo(BigDecimal.valueOf(0.35));
    }

    @Test
    @DisplayName("Should return correct name")
    void shouldReturnCorrectName() {
        // Given
        var freshnessConfig = new ScoringProperties.Freshness();
        var rule = new FreshnessScoringRule(freshnessConfig);

        // When
        var name = rule.getName();

        // Then
        assertThat(name).isEqualTo("FreshnessScoringRule");
    }
}
