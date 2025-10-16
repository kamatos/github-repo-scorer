package com.gerard.githubreposcorer.data;

import com.gerard.githubreposcorer.config.GitHubApiProperties;
import com.gerard.githubreposcorer.domain.model.GitHubRepository;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import com.gerard.githubreposcorer.domain.model.RepositorySearchResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubApiClientIntegrationTest {

    private static WireMockServer wireMockServer;
    private GitHubApiClient gitHubApiClient;
    private GitHubApiProperties gitHubApiProperties;

    @BeforeAll
    static void beforeAll() {
        // Start WireMock server on a random port
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        // Configure GitHubApiProperties to use WireMock server
        gitHubApiProperties = new GitHubApiProperties();
        gitHubApiProperties.setBaseUrl("http://localhost:" + wireMockServer.port());
        gitHubApiProperties.setToken("test-token");
        gitHubApiProperties.setTimeout(30000);

        RestClient restClient = RestClient.builder().build();
        gitHubApiClient = new GitHubApiClient(restClient, gitHubApiProperties);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Should successfully search repositories with valid response")
    void shouldSuccessfullySearchRepositoriesWithValidResponse() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .language("Java")
                .page(0)
                .size(10)
                .build();

        String mockResponse = """
                {
                    "total_count": 2,
                    "items": [
                        {
                            "name": "spring-boot",
                            "full_name": "spring-projects/spring-boot",
                            "html_url": "https://github.com/spring-projects/spring-boot",
                            "language": "Java",
                            "stargazers_count": 50000,
                            "forks_count": 30000,
                            "created_at": "2020-01-01T12:00:00Z",
                            "updated_at": "2023-12-01T15:30:00Z",
                            "owner": {
                                "login": "spring-projects"
                            }
                        },
                        {
                            "name": "spring-framework",
                            "full_name": "spring-projects/spring-framework",
                            "html_url": "https://github.com/spring-projects/spring-framework",
                            "language": "Java",
                            "stargazers_count": 40000,
                            "forks_count": 25000,
                            "created_at": "2019-06-01T10:00:00Z",
                            "updated_at": "2023-11-15T14:20:00Z",
                            "owner": {
                                "login": "spring-projects"
                            }
                        }
                    ]
                }
                """;

        // Setup WireMock stub
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo("org:spring-projects language:Java sort:stars"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("per_page", equalTo("10"))
                .withHeader("Accept", equalTo("application/vnd.github.v3+json"))
                .withHeader("Authorization", equalTo("token test-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.items()).hasSize(2);

        GitHubRepository firstRepo = result.items().getFirst();
        assertThat(firstRepo.getName()).isEqualTo("spring-boot");
        assertThat(firstRepo.getFullName()).isEqualTo("spring-projects/spring-boot");
        assertThat(firstRepo.getLanguage()).isEqualTo("Java");
        assertThat(firstRepo.getStars()).isEqualTo(50000);
        assertThat(firstRepo.getForks()).isEqualTo(30000);
        assertThat(firstRepo.getOwner()).isEqualTo("spring-projects");
        assertThat(firstRepo.getCreatedAt()).isEqualTo(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
        assertThat(firstRepo.getUpdatedAt()).isEqualTo(LocalDateTime.of(2023, 12, 1, 15, 30, 0));

        GitHubRepository secondRepo = result.items().get(1);
        assertThat(secondRepo.getName()).isEqualTo("spring-framework");
        assertThat(secondRepo.getFullName()).isEqualTo("spring-projects/spring-framework");
        assertThat(secondRepo.getLanguage()).isEqualTo("Java");
        assertThat(secondRepo.getStars()).isEqualTo(40000);
        assertThat(secondRepo.getForks()).isEqualTo(25000);
        assertThat(secondRepo.getOwner()).isEqualTo("spring-projects");

        // Verify WireMock interactions
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo("org:spring-projects language:Java sort:stars"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("per_page", equalTo("10"))
                .withHeader("Accept", equalTo("application/vnd.github.v3+json"))
                .withHeader("Authorization", equalTo("token test-token")));
    }

    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("nonexistent-org")
                .page(0)
                .size(10)
                .build();

        String emptyResponse = """
                {
                    "total_count": 0,
                    "items": []
                }
                """;

        // Setup WireMock stub
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyResponse)));

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("Should handle API error responses")
    void shouldHandleApiErrorResponses() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        // Setup WireMock stub to return 500 error
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Internal Server Error\"}")));

        // When & Then
        assertThatThrownBy(() -> gitHubApiClient.searchRepositories(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to fetch repositories from GitHub API");
    }

    @Test
    @DisplayName("Should handle rate limit responses")
    void shouldHandleRateLimitResponses() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        // Setup WireMock stub to return 429 rate limit error
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"API rate limit exceeded\"}")));

        // When & Then
        assertThatThrownBy(() -> gitHubApiClient.searchRepositories(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to fetch repositories from GitHub API");
    }

    @Test
    @DisplayName("Should work without authorization token")
    void shouldWorkWithoutAuthorizationToken() {
        // Given
        gitHubApiProperties.setToken("");
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        String mockResponse = """
                {
                    "total_count": 1,
                    "items": [
                        {
                            "name": "spring-boot",
                            "full_name": "spring-projects/spring-boot",
                            "html_url": "https://github.com/spring-projects/spring-boot",
                            "language": "Java",
                            "stargazers_count": 50000,
                            "forks_count": 30000,
                            "created_at": "2020-01-01T12:00:00Z",
                            "updated_at": "2023-12-01T15:30:00Z",
                            "owner": {
                                "login": "spring-projects"
                            }
                        }
                    ]
                }
                """;

        // Setup WireMock stub without Authorization header
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withHeader("Accept", equalTo("application/vnd.github.v3+json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);

        // Verify WireMock interactions - should not have Authorization header
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withHeader("Accept", equalTo("application/vnd.github.v3+json")));
    }

    @Test
    @DisplayName("Should handle complex search queries with all criteria")
    void shouldHandleComplexSearchQueriesWithAllCriteria() {
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

        String mockResponse = """
                {
                    "total_count": 1,
                    "items": [
                        {
                            "name": "spring-boot",
                            "full_name": "spring-projects/spring-boot",
                            "html_url": "https://github.com/spring-projects/spring-boot",
                            "language": "Java",
                            "stargazers_count": 50000,
                            "forks_count": 30000,
                            "created_at": "2020-01-01T12:00:00Z",
                            "updated_at": "2023-12-01T15:30:00Z",
                            "owner": {
                                "login": "spring-projects"
                            }
                        }
                    ]
                }
                """;

        // Setup WireMock stub with complex query
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo("org:spring-projects language:Java spring-boot created:>=2020-01-01 sort:stars"))
                .withQueryParam("page", equalTo("2"))
                .withQueryParam("per_page", equalTo("20"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // When
        RepositorySearchResponse result = gitHubApiClient.searchRepositories(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);

        // Verify WireMock interactions with complex query
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo("org:spring-projects language:Java spring-boot created:>=2020-01-01 sort:stars"))
                .withQueryParam("page", equalTo("2"))
                .withQueryParam("per_page", equalTo("20")));
    }

    @Test
    @DisplayName("Should handle malformed JSON response")
    void shouldHandleMalformedJsonResponse() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
                .org("spring-projects")
                .page(0)
                .size(10)
                .build();

        // Setup WireMock stub to return malformed JSON
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"invalid\": json}")));

        // When & Then
        assertThatThrownBy(() -> gitHubApiClient.searchRepositories(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to fetch repositories from GitHub API");
    }
}
