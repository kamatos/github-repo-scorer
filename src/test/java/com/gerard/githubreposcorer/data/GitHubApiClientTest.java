package com.gerard.githubreposcorer.data;

import com.gerard.githubreposcorer.config.GitHubApiProperties;
import com.gerard.githubreposcorer.domain.model.GitHubRepository;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import com.gerard.githubreposcorer.domain.model.RepositorySearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubApiClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private GitHubApiProperties gitHubApiProperties;
    private GitHubApiClient gitHubApiClient;
    private String capturedUrl;

    @BeforeEach
    void setUp() {
        gitHubApiProperties = new GitHubApiProperties();
        gitHubApiProperties.setBaseUrl("https://api.github.com");
        gitHubApiProperties.setToken("test-token");

        gitHubApiClient = new GitHubApiClient(restClient, gitHubApiProperties);
    }

    @Test
    @DisplayName("Should return search response with valid request")
    void shouldReturnSearchResponseWithValidRequest() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .language("Java")
                .page(0)
                .size(10)
                .build();

        Map<String, Object> mockResponse = createMockGitHubApiResponse();
        setupMockChain(mockResponse);

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.items()).hasSize(2);

        GitHubRepository firstRepo = result.items().get(0);
        assertThat(firstRepo.getName()).isEqualTo("spring-boot");
        assertThat(firstRepo.getFullName()).isEqualTo("spring-projects/spring-boot");
        assertThat(firstRepo.getLanguage()).isEqualTo("Java");
        assertThat(firstRepo.getStars()).isEqualTo(50000);
        assertThat(firstRepo.getForks()).isEqualTo(30000);
        assertThat(firstRepo.getOwner()).isEqualTo("spring-projects");

        // Verify RestClient interactions
        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(anyString());
        verify(requestHeadersSpec).header(eq("Accept"), eq("application/vnd.github.v3+json"));
        verify(requestHeadersSpec).header(eq("Authorization"), eq("token test-token"));
        
        assertThat(capturedUrl).isNotNull();
        assertThat(capturedUrl).contains("https://api.github.com/search/repositories");
        assertThat(capturedUrl).contains("q=org:spring-projects language:Java sort:stars");
        assertThat(capturedUrl).contains("page=1"); // GitHub uses 1-based pagination
        assertThat(capturedUrl).contains("per_page=10");
    }

    @Test
    @DisplayName("Should not add authorization header with empty token")
    void shouldNotAddAuthorizationHeaderWithEmptyToken() {
        // Given
        gitHubApiProperties.setToken("");
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        Map<String, Object> mockResponse = createMockGitHubApiResponse();
        setupMockChain(mockResponse);

        // When
        gitHubApiClient.searchRepositories(request);

        // Then
        verify(requestHeadersSpec, never()).header(eq("Authorization"), anyString());
    }

    @Test
    @DisplayName("Should not add authorization header with null token")
    void shouldNotAddAuthorizationHeaderWithNullToken() {
        // Given
        gitHubApiProperties.setToken(null);
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        Map<String, Object> mockResponse = createMockGitHubApiResponse();
        setupMockChain(mockResponse);

        // When
        gitHubApiClient.searchRepositories(request);

        // Then
        verify(requestHeadersSpec, never()).header(eq("Authorization"), anyString());
    }

    @Test
    @DisplayName("Should build correct query with all search criteria")
    void shouldBuildCorrectQueryWithAllSearchCriteria() {
        // Given
        LocalDateTime creationDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .language("Java")
                .repo("spring-boot")
                .earliestCreationDate(creationDate)
                .page(1)
                .size(20)
                .build();

        Map<String, Object> mockResponse = createMockGitHubApiResponse();
        setupMockChain(mockResponse);

        // When
        gitHubApiClient.searchRepositories(request);

        // Then
        verify(requestHeadersUriSpec).uri(anyString());
        verify(requestHeadersSpec).header(eq("Accept"), eq("application/vnd.github.v3+json"));
        verify(requestHeadersSpec).header(eq("Authorization"), eq("token test-token"));
        
        // Verify actual query parameters sent to GitHub API
        assertThat(capturedUrl).isNotNull();
        assertThat(capturedUrl).contains("https://api.github.com/search/repositories");
        assertThat(capturedUrl).contains("q=org:spring-projects language:Java spring-boot created:>=2020-01-01 sort:stars");
        assertThat(capturedUrl).contains("page=2"); // GitHub uses 1-based pagination (page 1 -> page 2)
        assertThat(capturedUrl).contains("per_page=20");
    }

    @Test
    @DisplayName("Should return empty result with empty response")
    void shouldReturnEmptyResultWithEmptyResponse() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("nonexistent-org")
                .page(0)
                .size(10)
                .build();

        Map<String, Object> emptyResponse = Map.of("total_count", 0, "items", List.of());
        setupMockChain(emptyResponse);

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty result with null response")
    void shouldReturnEmptyResultWithNullResponse() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        setupMockChain(null);

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty result when response missing items")
    void shouldReturnEmptyResultWhenResponseMissingItems() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        Map<String, Object> invalidResponse = Map.of("total_count", 5);
        setupMockChain(invalidResponse);

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("Should throw runtime exception when RestClient throws exception")
    void shouldThrowRuntimeExceptionWhenRestClientThrowsException() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenThrow(new RestClientException("API Error"));

        // When & Then
        assertThatThrownBy(() -> gitHubApiClient.searchRepositories(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to fetch repositories from GitHub API")
                .hasCauseInstanceOf(RestClientException.class);
    }

    @Test
    @DisplayName("Should build basic query with minimal request")
    void shouldBuildBasicQueryWithMinimalRequest() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .page(0)
                .size(10)
                .build();

        Map<String, Object> mockResponse = createMockGitHubApiResponse();
        setupMockChain(mockResponse);

        // When
        gitHubApiClient.searchRepositories(request);

        // Then
        verify(requestHeadersUriSpec).uri(anyString());
        verify(requestHeadersSpec).header(eq("Accept"), eq("application/vnd.github.v3+json"));
        verify(requestHeadersSpec).header(eq("Authorization"), eq("token test-token"));
        
        // Verify actual query parameters sent to GitHub API
        assertThat(capturedUrl).isNotNull();
        assertThat(capturedUrl).contains("https://api.github.com/search/repositories");
        assertThat(capturedUrl).contains("q=sort:stars"); // Only sort:stars should be present
        assertThat(capturedUrl).contains("page=1"); // GitHub uses 1-based pagination
        assertThat(capturedUrl).contains("per_page=10");
    }

    @Test
    @DisplayName("Should not include empty string criteria in query")
    void shouldNotIncludeEmptyStringCriteriaInQuery() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("")
                .language("")
                .repo("")
                .page(0)
                .size(10)
                .build();

        Map<String, Object> mockResponse = createMockGitHubApiResponse();
        setupMockChain(mockResponse);

        // When
        gitHubApiClient.searchRepositories(request);

        // Then
        verify(requestHeadersUriSpec).uri(anyString());
        verify(requestHeadersSpec).header(eq("Accept"), eq("application/vnd.github.v3+json"));
        verify(requestHeadersSpec).header(eq("Authorization"), eq("token test-token"));
        
        // Verify actual query parameters sent to GitHub API
        assertThat(capturedUrl).isNotNull();
        assertThat(capturedUrl).contains("https://api.github.com/search/repositories");
        assertThat(capturedUrl).contains("q=sort:stars"); // Only sort:stars should be present
        assertThat(capturedUrl).contains("page=1"); // GitHub uses 1-based pagination
        assertThat(capturedUrl).contains("per_page=10");
        // Verify empty criteria are not included
        assertThat(capturedUrl).doesNotContain("org:");
        assertThat(capturedUrl).doesNotContain("language:");
    }

    @Test
    @DisplayName("Should not include null criteria in query")
    void shouldNotIncludeNullCriteriaInQuery() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org(null)
                .language(null)
                .repo(null)
                .earliestCreationDate(null)
                .page(0)
                .size(10)
                .build();

        Map<String, Object> mockResponse = createMockGitHubApiResponse();
        setupMockChain(mockResponse);

        // When
        gitHubApiClient.searchRepositories(request);

        // Then
        verify(requestHeadersUriSpec).uri(anyString());
        verify(requestHeadersSpec).header(eq("Accept"), eq("application/vnd.github.v3+json"));
        verify(requestHeadersSpec).header(eq("Authorization"), eq("token test-token"));
        
        // Verify actual query parameters sent to GitHub API
        assertThat(capturedUrl).isNotNull();
        assertThat(capturedUrl).contains("https://api.github.com/search/repositories");
        assertThat(capturedUrl).contains("q=sort:stars"); // Only sort:stars should be present
        assertThat(capturedUrl).contains("page=1"); // GitHub uses 1-based pagination
        assertThat(capturedUrl).contains("per_page=10");
        // Verify null criteria are not included
        assertThat(capturedUrl).doesNotContain("org:");
        assertThat(capturedUrl).doesNotContain("language:");
        assertThat(capturedUrl).doesNotContain("created:>=");
    }

    @Test
    @DisplayName("Should map repository fields correctly")
    void shouldMapRepositoryFieldsCorrectly() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("test-org")
                .page(0)
                .size(1)
                .build();

        Map<String, Object> mockResponse = createDetailedMockResponse();
        setupMockChain(mockResponse);

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result.items()).hasSize(1);
        GitHubRepository repo = result.items().get(0);
        
        assertThat(repo.getName()).isEqualTo("test-repo");
        assertThat(repo.getFullName()).isEqualTo("test-org/test-repo");
        assertThat(repo.getHtmlUrl()).isEqualTo("https://github.com/test-org/test-repo");
        assertThat(repo.getLanguage()).isEqualTo("Java");
        assertThat(repo.getStars()).isEqualTo(1000);
        assertThat(repo.getForks()).isEqualTo(500);
        assertThat(repo.getOwner()).isEqualTo("test-org");
        assertThat(repo.getCreatedAt()).isEqualTo(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
        assertThat(repo.getUpdatedAt()).isEqualTo(LocalDateTime.of(2023, 12, 1, 15, 30, 0));
    }

    private void setupMockChain(Map<String, Object> responseBody) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenAnswer(invocation -> {
            capturedUrl = invocation.getArgument(0);
            return requestHeadersSpec;
        });
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(responseBody);
    }

    private Map<String, Object> createMockGitHubApiResponse() {
        Map<String, Object> repo1 = Map.of(
                "name", "spring-boot",
                "full_name", "spring-projects/spring-boot",
                "html_url", "https://github.com/spring-projects/spring-boot",
                "language", "Java",
                "stargazers_count", 50000,
                "forks_count", 30000,
                "created_at", "2020-01-01T12:00:00Z",
                "updated_at", "2023-12-01T15:30:00Z",
                "owner", Map.of("login", "spring-projects")
        );

        Map<String, Object> repo2 = Map.of(
                "name", "spring-framework",
                "full_name", "spring-projects/spring-framework",
                "html_url", "https://github.com/spring-projects/spring-framework",
                "language", "Java",
                "stargazers_count", 40000,
                "forks_count", 25000,
                "created_at", "2019-06-01T10:00:00Z",
                "updated_at", "2023-11-15T14:20:00Z",
                "owner", Map.of("login", "spring-projects")
        );

        return Map.of(
                "total_count", 2,
                "items", List.of(repo1, repo2)
        );
    }

    private Map<String, Object> createDetailedMockResponse() {
        Map<String, Object> repo = Map.of(
                "name", "test-repo",
                "full_name", "test-org/test-repo",
                "html_url", "https://github.com/test-org/test-repo",
                "language", "Java",
                "stargazers_count", 1000,
                "forks_count", 500,
                "created_at", "2020-01-01T12:00:00Z",
                "updated_at", "2023-12-01T15:30:00Z",
                "owner", Map.of("login", "test-org")
        );

        return Map.of(
                "total_count", 1,
                "items", List.of(repo)
        );
    }
}