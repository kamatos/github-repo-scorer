package com.gerard.githubreposcorer.service;

import com.gerard.githubreposcorer.config.ScoringProperties;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "scoring.strategy.version=v1",
    "scoring.stars.cap=10000",
    "scoring.stars.weight=0.5",
    "scoring.forks.cap=10000",
    "scoring.forks.weight=0.5"
})
class ScoringServiceTest {

    @Autowired
    private ScoringService scoringService;

    @Autowired
    private ScoringProperties scoringProperties;

    @Test
    @DisplayName("Should calculate score correctly with valid configuration")
    void shouldCalculateScoreCorrectlyWithValidConfiguration() {
        // Given
        ScoringContext context = ScoringContext.builder().stars(1000).forks(500).build();

        // When
        BigDecimal actualScore = scoringService.calculateScore(context);

        // Then
        assertThat(actualScore).isGreaterThan(BigDecimal.ZERO);
        assertThat(actualScore).isLessThanOrEqualTo(BigDecimal.ONE);
    }

    @ParameterizedTest(name = "Repository: {0} - Stars: {1}, Forks: {2} -> Expected Score: {3}")
    @MethodSource("repos")
    @DisplayName("Should calculate correct scores for top repositories")
    void shouldCalculateCorrectScoresForTopRepositories(String repoName, int stars, int forks, double expectedScore) {
        // Given
        ScoringContext context = ScoringContext.builder().stars(stars).forks(forks).build();

        // When
        BigDecimal actualScore = scoringService.calculateScore(context);

        // Then
        assertThat(actualScore).isEqualTo(BigDecimal.valueOf(expectedScore));
    }

    @Test
    @DisplayName("Should handle zero values correctly")
    void shouldHandleZeroValuesCorrectly() {
        // Given
        ScoringContext context = ScoringContext.builder().stars(0).forks(0).build();

        // When
        BigDecimal actualScore = scoringService.calculateScore(context);

        // Then
        assertThat(actualScore).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Should handle very large values correctly")
    void shouldHandleVeryLargeValuesCorrectly() {
        // Given
        ScoringContext context = ScoringContext.builder().stars(1_000_000).forks(500_000).build();

        // When
        BigDecimal actualScore = scoringService.calculateScore(context);

        // then
        assertThat(actualScore).isGreaterThan(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should verify scoring properties are correctly configured")
    void shouldVerifyScoringPropertiesAreCorrectlyConfigured() {
        assertThat(scoringProperties.getStrategy().getVersion()).isEqualTo("v1");
        assertThat(scoringProperties.getStars().getCap()).isEqualTo(10000);
        assertThat(scoringProperties.getStars().getWeight()).isEqualTo(0.5);
        assertThat(scoringProperties.getForks().getCap()).isEqualTo(10000);
        assertThat(scoringProperties.getForks().getWeight()).isEqualTo(0.5);
    }

    /**
     * Test data for top repositories with their stars, forks, and expected scores.
     * Format: (repository name, stars count, forks count, expected score)
     */
    static Stream<Arguments> repos() {
        return Stream.of(
                Arguments.of("freeCodeCamp/freeCodeCamp", 429_679, 41_894, 1.28),
                Arguments.of("facebook/react", 234_700, 48_700, 1.26),
                Arguments.of("kubernetes/kubernetes", 109_211, 40_562, 1.21),
                Arguments.of("spring-projects/spring-boot", 78_600, 41_500, 1.19)
        );
    }
}
