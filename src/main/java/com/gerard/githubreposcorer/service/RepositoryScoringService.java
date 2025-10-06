package com.gerard.githubreposcorer.service;

import com.gerard.githubreposcorer.data.RepositoriesSource;
import com.gerard.githubreposcorer.domain.model.GitHubRepository;
import com.gerard.githubreposcorer.domain.model.RepositoryScore;
import com.gerard.githubreposcorer.domain.model.RepositoryScorePage;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import com.gerard.githubreposcorer.domain.model.RepositorySearchResponse;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryScoringService {

    private final RepositoriesSource repositoriesSource;
    private final ScoringService scoringService;

    public RepositoryScorePage scoreRepositories(RepositorySearchRequest request) {
        // Fetch repositories from the configured source
        RepositorySearchResponse repositorySearchResponse = repositoriesSource.searchRepositories(request);

        // Score each repository
        List<RepositoryScore> scoredRepositories = repositorySearchResponse.items().stream()
                .map(this::scoreRepository)
                .collect(Collectors.toList());

        return RepositoryScorePage.builder()
                .content(scoredRepositories)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(scoredRepositories.size())
                .totalPages(repositorySearchResponse.totalCount())
                .build();
    }

    private RepositoryScore scoreRepository(GitHubRepository repository) {
        // Calculate days since last update
        int daysSinceUpdate = repository.getUpdatedAt() != null 
            ? (int) ChronoUnit.DAYS.between(repository.getUpdatedAt(), LocalDateTime.now())
            : 0;

        // Create scoring context
        ScoringContext context = ScoringContext.builder()
                .stars(repository.getStars())
                .forks(repository.getForks())
                .daysSinceUpdate(daysSinceUpdate)
                .build();

        // Calculate score
        BigDecimal score = scoringService.calculateScore(context);

        return RepositoryScore.builder()
                .name(repository.getName())
                .url(repository.getHtmlUrl())
                .score(score)
                .build();
    }
}