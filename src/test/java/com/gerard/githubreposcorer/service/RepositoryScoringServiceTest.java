package com.gerard.githubreposcorer.service;

import com.gerard.githubreposcorer.data.RepositoriesSource;
import com.gerard.githubreposcorer.domain.model.GitHubRepository;
import com.gerard.githubreposcorer.domain.model.RepositoryScore;
import com.gerard.githubreposcorer.domain.model.RepositoryScorePage;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import com.gerard.githubreposcorer.domain.model.RepositorySearchResponse;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryScoringServiceTest {

    @Mock
    private RepositoriesSource repositoriesSource;

    @Mock
    private ScoringService scoringService;

    @InjectMocks
    private RepositoryScoringService repositoryScoringService;

    private RepositorySearchRequest searchRequest;
    private GitHubRepository sampleRepository;
    private RepositorySearchResponse searchResponse;

    @BeforeEach
    void setUp() {
        searchRequest = RepositorySearchRequest.builder()
                .language("Java")
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        sampleRepository = GitHubRepository.builder()
                .name("spring-boot")
                .htmlUrl("https://github.com/spring-projects/spring-boot")
                .stars(78600)
                .forks(41500)
                .build();

        searchResponse = new RepositorySearchResponse(
                1,
                List.of(sampleRepository)
        );
    }

    @Test
    @DisplayName("Should score repositories successfully with valid request")
    void shouldScoreRepositoriesSuccessfullyWithValidRequest() {
        // Given
        when(repositoriesSource.searchRepositories(searchRequest)).thenReturn(searchResponse);
        when(scoringService.calculateScore(any(ScoringContext.class)))
                .thenReturn(BigDecimal.valueOf(0.85));

        // When
        RepositoryScorePage result = repositoryScoringService.scoreRepositories(searchRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);

        RepositoryScore scoredRepo = result.getContent().get(0);
        assertThat(scoredRepo.getName()).isEqualTo("spring-boot");
        assertThat(scoredRepo.getUrl()).isEqualTo("https://github.com/spring-projects/spring-boot");
        assertThat(scoredRepo.getScore()).isEqualTo(BigDecimal.valueOf(0.85));

        verify(repositoriesSource).searchRepositories(searchRequest);
        verify(scoringService).calculateScore(any(ScoringContext.class));
    }

    @Test
    @DisplayName("Should handle multiple repositories correctly")
    void shouldHandleMultipleRepositoriesCorrectly() {
        // Given
        GitHubRepository repo1 = GitHubRepository.builder()
                .name("spring-boot")
                .htmlUrl("https://github.com/spring-projects/spring-boot")
                .stars(78600)
                .forks(41500)
                .build();

        GitHubRepository repo2 = GitHubRepository.builder()
                .name("spring-framework")
                .htmlUrl("https://github.com/spring-projects/spring-framework")
                .stars(45000)
                .forks(32000)
                .build();

        RepositorySearchResponse multiRepoResponse = new RepositorySearchResponse(
                2,
                List.of(repo1, repo2)
        );

        when(repositoriesSource.searchRepositories(searchRequest)).thenReturn(multiRepoResponse);
        when(scoringService.calculateScore(any(ScoringContext.class)))
                .thenReturn(BigDecimal.valueOf(0.75))
                .thenReturn(BigDecimal.valueOf(0.65));

        // When
        RepositoryScorePage result = repositoryScoringService.scoreRepositories(searchRequest);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);

        assertThat(result.getContent().get(0).getName()).isEqualTo("spring-boot");
        assertThat(result.getContent().get(0).getScore()).isEqualTo(BigDecimal.valueOf(0.75));
        assertThat(result.getContent().get(1).getName()).isEqualTo("spring-framework");
        assertThat(result.getContent().get(1).getScore()).isEqualTo(BigDecimal.valueOf(0.65));

        verify(scoringService, times(2)).calculateScore(any(ScoringContext.class));
    }

    @Test
    @DisplayName("Should handle empty repository list")
    void shouldHandleEmptyRepositoryList() {
        // Given
        RepositorySearchResponse emptyResponse = new RepositorySearchResponse(
                0,
                List.of()
        );
        when(repositoriesSource.searchRepositories(searchRequest)).thenReturn(emptyResponse);

        // When
        RepositoryScorePage result = repositoryScoringService.scoreRepositories(searchRequest);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        verify(repositoriesSource).searchRepositories(searchRequest);
        verify(scoringService, times(0)).calculateScore(any(ScoringContext.class));
    }

    @Test
    @DisplayName("Should create correct scoring context for repository")
    void shouldCreateCorrectScoringContextForRepository() {
        // Given
        when(repositoriesSource.searchRepositories(searchRequest)).thenReturn(searchResponse);
        when(scoringService.calculateScore(any(ScoringContext.class)))
                .thenReturn(BigDecimal.valueOf(0.90));

        // When
        repositoryScoringService.scoreRepositories(searchRequest);

        // Then
        verify(scoringService).calculateScore(argThat(context -> 
                context.getStars() == 78600 && 
                context.getForks() == 41500
        ));
    }

    @Test
    @DisplayName("Should handle different scoring results correctly")
    void shouldHandleDifferentScoringResultsCorrectly() {
        // Given
        GitHubRepository highScoreRepo = GitHubRepository.builder()
                .name("high-score-repo")
                .htmlUrl("https://github.com/example/high-score-repo")
                .stars(100000)
                .forks(50000)
                .build();

        GitHubRepository lowScoreRepo = GitHubRepository.builder()
                .name("low-score-repo")
                .htmlUrl("https://github.com/example/low-score-repo")
                .stars(100)
                .forks(50)
                .build();

        RepositorySearchResponse mixedResponse = new RepositorySearchResponse(
                2,
                List.of(highScoreRepo, lowScoreRepo)
        );

        when(repositoriesSource.searchRepositories(searchRequest)).thenReturn(mixedResponse);
        when(scoringService.calculateScore(any(ScoringContext.class)))
                .thenReturn(BigDecimal.valueOf(1.25))  // High score
                .thenReturn(BigDecimal.valueOf(0.15)); // Low score

        // When
        RepositoryScorePage result = repositoryScoringService.scoreRepositories(searchRequest);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getScore()).isEqualTo(BigDecimal.valueOf(1.25));
        assertThat(result.getContent().get(1).getScore()).isEqualTo(BigDecimal.valueOf(0.15));
    }

    @Test
    @DisplayName("Should preserve request pagination parameters in response")
    void shouldPreserveRequestPaginationParametersInResponse() {
        // Given
        RepositorySearchRequest customRequest = RepositorySearchRequest.builder()
                .language("Python")
                .page(2)
                .size(5)
                .build();

        when(repositoriesSource.searchRepositories(customRequest)).thenReturn(searchResponse);
        when(scoringService.calculateScore(any(ScoringContext.class)))
                .thenReturn(BigDecimal.valueOf(0.80));

        // When
        RepositoryScorePage result = repositoryScoringService.scoreRepositories(customRequest);

        // Then
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle repository with zero stars and forks")
    void shouldHandleRepositoryWithZeroStarsAndForks() {
        // Given
        GitHubRepository zeroRepo = GitHubRepository.builder()
                .name("zero-repo")
                .htmlUrl("https://github.com/example/zero-repo")
                .stars(0)
                .forks(0)
                .build();

        RepositorySearchResponse zeroResponse = new RepositorySearchResponse(
                1,
                List.of(zeroRepo)
        );

        when(repositoriesSource.searchRepositories(searchRequest)).thenReturn(zeroResponse);
        when(scoringService.calculateScore(any(ScoringContext.class)))
                .thenReturn(BigDecimal.ZERO);

        // When
        RepositoryScorePage result = repositoryScoringService.scoreRepositories(searchRequest);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getScore()).isEqualTo(BigDecimal.ZERO);
        
        verify(scoringService).calculateScore(argThat(context -> 
                context.getStars() == 0 && 
                context.getForks() == 0
        ));
    }

    @Test
    @DisplayName("Should handle repository with very large star and fork counts")
    void shouldHandleRepositoryWithVeryLargeStarAndForkCounts() {
        // Given
        GitHubRepository largeRepo = GitHubRepository.builder()
                .name("large-repo")
                .htmlUrl("https://github.com/example/large-repo")
                .stars(1_000_000)
                .forks(500_000)
                .build();

        RepositorySearchResponse largeResponse = new RepositorySearchResponse(
                1,
                List.of(largeRepo)
        );

        when(repositoriesSource.searchRepositories(searchRequest)).thenReturn(largeResponse);
        when(scoringService.calculateScore(any(ScoringContext.class)))
                .thenReturn(BigDecimal.valueOf(2.50));

        // When
        RepositoryScorePage result = repositoryScoringService.scoreRepositories(searchRequest);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getScore()).isEqualTo(BigDecimal.valueOf(2.50));
        
        verify(scoringService).calculateScore(argThat(context -> 
                context.getStars() == 1_000_000 && 
                context.getForks() == 500_000
        ));
    }
}
