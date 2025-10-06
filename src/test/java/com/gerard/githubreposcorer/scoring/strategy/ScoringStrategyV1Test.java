package com.gerard.githubreposcorer.scoring.strategy;

import com.gerard.githubreposcorer.config.ScoringProperties;
import com.gerard.githubreposcorer.scoring.exception.InvalidWeightsException;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ScoringStrategyV1Test {

    @Test
    @DisplayName("Should calculate score successfully with valid weights")
    void shouldCalculateScoreSuccessfullyWithValidWeights() {
        // Given
        var scoringProperties = new ScoringProperties();
        scoringProperties.setStars(starsConfig(0.45, 10000));
        scoringProperties.setForks(forksConfig(0.2, 10000));
        scoringProperties.setFreshness(freshnessConfig(0.35, 90));

        var scoringStrategy = new ScoringStrategyV1(scoringProperties, ForkJoinPool.commonPool());
        scoringStrategy.afterSingletonsInstantiated();

        ScoringContext context = ScoringContext.builder()
                .stars(1000)
                .forks(500)
                .daysSinceUpdate(30)
                .build();

        // When
        BigDecimal score = scoringStrategy.calculateScore(context);

        // Then
        assertThat(score).isNotNull();
        assertThat(score).isBetween(BigDecimal.ZERO, BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should throw InvalidWeightsException when weights don't sum to 1")
    void shouldThrowInvalidWeightsExceptionWhenWeightsDontSumToOne() {
        // Given
        var scoringProperties = new ScoringProperties();
        scoringProperties.setStars(starsConfig(0.3, 10000));
        scoringProperties.setForks(forksConfig(0.5, 10000));
        scoringProperties.setFreshness(freshnessConfig(0.1, 90));

        var scoringStrategy = new ScoringStrategyV1(scoringProperties, ForkJoinPool.commonPool());

        // When & Then
        assertThatThrownBy(scoringStrategy::afterSingletonsInstantiated)
                .isInstanceOf(InvalidWeightsException.class)
                .hasMessageContaining("Weights must sum to 1.0, but sum is: 0.9");
    }

    @Test
    @DisplayName("Should throw InvalidWeightsException when individual weight exceeds 1")
    void shouldThrowInvalidWeightsExceptionWhenIndividualWeightExceedsOne() {
        // Given
        var scoringProperties = new ScoringProperties();
        scoringProperties.setStars(starsConfig(1.5, 10000));
        scoringProperties.setForks(forksConfig(0.2, 10000));
        scoringProperties.setFreshness(freshnessConfig(0.35, 90));

        var scoringStrategy = new ScoringStrategyV1(scoringProperties, ForkJoinPool.commonPool());

        // When & Then
        assertThatThrownBy(scoringStrategy::afterSingletonsInstantiated)
                .isInstanceOf(InvalidWeightsException.class)
                .hasMessage("Weights must sum to 1.0, but sum is: 2.05");
    }

    @Test
    @DisplayName("Should return correct version")
    void shouldReturnCorrectVersion() {
        // When
        var scoringStrategy = new ScoringStrategyV1(mock(ScoringProperties.class), ForkJoinPool.commonPool());
        String version = scoringStrategy.getVersion();

        // Then
        assertThat(version).isEqualTo("v1");
    }

    private static ScoringProperties.Stars starsConfig(double weight, int cap) {
        ScoringProperties.Stars stars = new ScoringProperties.Stars();
        stars.setWeight(weight);
        stars.setCap(cap);
        return stars;
    }

    private static ScoringProperties.Forks forksConfig(double weight, int cap) {
        ScoringProperties.Forks forks = new ScoringProperties.Forks();
        forks.setWeight(weight);
        forks.setCap(cap);
        return forks;
    }

    private static ScoringProperties.Freshness freshnessConfig(double weight, int halfLifeDays) {
        ScoringProperties.Freshness freshness = new ScoringProperties.Freshness();
        freshness.setWeight(weight);
        freshness.setHalfLifeDays(halfLifeDays);
        return freshness;
    }
}
