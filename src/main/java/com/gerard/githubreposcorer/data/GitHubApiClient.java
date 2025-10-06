package com.gerard.githubreposcorer.data;

import com.gerard.githubreposcorer.config.GitHubApiProperties;
import com.gerard.githubreposcorer.domain.model.GitHubRepository;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import com.gerard.githubreposcorer.domain.model.RepositorySearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class GitHubApiClient implements RepositoriesSource {

    private final RestClient restClient;
    private final GitHubApiProperties gitHubApiProperties;

    @Override
    public RepositorySearchResponse searchRepositories(RepositorySearchRequest request) {
        String query = buildSearchQuery(request);
        String url = buildSearchUrl(query, request.getPage(), request.getSize());

        try {
            RestClient.RequestHeadersSpec<?> requestSpec = restClient.get()
                    .uri(url)
                    .header("Accept", "application/vnd.github.v3+json");

            if (gitHubApiProperties.getToken() != null && !gitHubApiProperties.getToken().isEmpty()) {
                requestSpec = requestSpec.header("Authorization", "token " + gitHubApiProperties.getToken());
            }

            Map<String, Object> responseBody = requestSpec.retrieve().body(Map.class);

            if (responseBody != null && responseBody.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) responseBody.get("items");
                int totalCount = (Integer) responseBody.getOrDefault("total_count", 0);
                List<GitHubRepository> repositories = items.stream()
                        .map(this::mapToGitHubRepository)
                        .toList();

                return new RepositorySearchResponse(totalCount, repositories);
            }
            return new RepositorySearchResponse(0, List.of());
        } catch (Exception e) {
            log.error("Error calling GitHub API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch repositories from GitHub API", e);
        }
    }

    private String buildSearchQuery(RepositorySearchRequest request) {
        StringBuilder query = new StringBuilder();

        if (request.getOrg() != null && !request.getOrg().isEmpty()) {
            query.append("org:").append(request.getOrg()).append(" ");
        }

        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            query.append("language:").append(request.getLanguage()).append(" ");
        }

        if (request.getRepo() != null && !request.getRepo().isEmpty()) {
            query.append(request.getRepo()).append(" ");
        }

        if (request.getEarliestCreationDate() != null) {
            String dateStr = request.getEarliestCreationDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            query.append("created:>=").append(dateStr).append(" ");
        }

        query.append("sort:stars");

        return query.toString().trim();
    }

    private String buildSearchUrl(String query, int page, int size) {
        return UriComponentsBuilder.fromUriString(gitHubApiProperties.getBaseUrl() + "/search/repositories")
                .queryParam("q", query)
                .queryParam("page", page + 1) // GitHub API uses 1-based pagination
                .queryParam("per_page", size)
                .build()
                .toUriString();
    }

    private GitHubRepository mapToGitHubRepository(Map<String, Object> item) {
        Map<String, Object> owner = (Map<String, Object>) item.get("owner");

        return GitHubRepository.builder()
                .name((String) item.get("name"))
                .fullName((String) item.get("full_name"))
                .htmlUrl((String) item.get("html_url"))
                .language((String) item.get("language"))
                .stars((Integer) item.get("stargazers_count"))
                .forks((Integer) item.get("forks_count"))
                .createdAt(parseDateTime((String) item.get("created_at")))
                .updatedAt(parseDateTime((String) item.get("updated_at")))
                .owner((String) owner.get("login"))
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        return LocalDateTime.parse(
                dateTimeStr.substring(0, 19),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        );
    }
}