package com.gerard.githubreposcorer.web.controller;

import com.gerard.githubreposcorer.api.model.RepositoryScore;
import com.gerard.githubreposcorer.api.model.RepositoryScorePage;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import com.gerard.githubreposcorer.service.RepositoryScoringService;
import com.gerard.githubreposcorer.web.mapper.RepositoryScoringMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepositoryScoringController.class)
@Import(RepositoryScoringMapper.class)
class RepositoryScoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepositoryScoringService repositoryScoringService;

    private com.gerard.githubreposcorer.domain.model.RepositoryScorePage mockDomainPage;
    private RepositoryScorePage expectedApiPage;

    @BeforeEach
    void setUp() {
        mockDomainPage = createMockDomainPage();

        // Setup expected API response
        RepositoryScore apiScore1 = new RepositoryScore();
        apiScore1.setName("spring-projects/spring-boot");
        apiScore1.setUrl(java.net.URI.create("https://github.com/spring-projects/spring-boot"));
        apiScore1.setScore(1.25f);

        RepositoryScore apiScore2 = new RepositoryScore();
        apiScore2.setName("facebook/react");
        apiScore2.setUrl(java.net.URI.create("https://github.com/facebook/react"));
        apiScore2.setScore(1.15f);

        expectedApiPage = new RepositoryScorePage();
        expectedApiPage.setContent(List.of(apiScore1, apiScore2));
        expectedApiPage.setPage(0);
        expectedApiPage.setSize(20);
        expectedApiPage.setTotalElements(2);
        expectedApiPage.setTotalPages(1);

    }

    @Test
    @DisplayName("Should return scored repositories with default parameters")
    void shouldReturnScoredRepositoriesWithDefaultParameters() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(mockDomainPage);

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("spring-projects/spring-boot"))
                .andExpect(jsonPath("$.content[0].url").value("https://github.com/spring-projects/spring-boot"))
                .andExpect(jsonPath("$.content[0].score").value(1.25))
                .andExpect(jsonPath("$.content[1].name").value("facebook/react"))
                .andExpect(jsonPath("$.content[1].url").value("https://github.com/facebook/react"))
                .andExpect(jsonPath("$.content[1].score").value(1.15))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Should return scored repositories with all query parameters")
    void shouldReturnScoredRepositoriesWithAllQueryParameters() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(mockDomainPage);

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("earliestCreationDate", "2024-01-01T00:00:00Z")
                        .param("language", "java")
                        .param("org", "spring-projects")
                        .param("repo", "spring-boot")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Should return empty page when no repositories found")
    void shouldReturnEmptyPageWhenNoRepositoriesFound() throws Exception {
        // Given
        com.gerard.githubreposcorer.domain.model.RepositoryScorePage emptyDomainPage =
                com.gerard.githubreposcorer.domain.model.RepositoryScorePage.builder()
                        .content(List.of())
                        .page(0)
                        .size(20)
                        .totalElements(0)
                        .totalPages(0)
                        .build();

        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(emptyDomainPage);


        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() throws Exception {
        // Given
        com.gerard.githubreposcorer.domain.model.RepositoryScorePage paginatedDomainPage =
                createPaginatedMockDomainPage(1, 1, 2, 2);

        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(paginatedDomainPage);


        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("page", "1")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("spring-projects/spring-boot"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("Should handle date parameter correctly")
    void shouldHandleDateParameterCorrectly() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(mockDomainPage);

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("earliestCreationDate", "2024-01-01T00:00:00Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Should handle invalid date format with 400 error")
    void shouldHandleInvalidDateFormatWith400Error() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("earliestCreationDate", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("Should handle negative page parameter with 400 error")
    void shouldHandleNegativePageParameterWith400Error() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"));
    }

    @Test
    @DisplayName("Should handle size parameter out of range with 400 error")
    void shouldHandleSizeParameterOutOfRangeWith500Error() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("size", "101") // Max is 100
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"));
    }

    @Test
    @DisplayName("Should handle zero size parameter with 400 error")
    void shouldHandleZeroSizeParameterWith500Error() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("size", "0") // Min is 1
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"));
    }

    @Test
    @DisplayName("Should handle service exception with 500 error")
    void shouldHandleServiceExceptionWith500Error() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Unexpected error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 error")
    void shouldHandleIllegalArgumentExceptionWith400Error() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid argument"));
    }

    @Test
    @DisplayName("Should handle language filter correctly")
    void shouldHandleLanguageFilterCorrectly() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(mockDomainPage);

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("language", "java")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Should handle organization filter correctly")
    void shouldHandleOrganizationFilterCorrectly() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(mockDomainPage);

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("org", "spring-projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Should handle repository name filter correctly")
    void shouldHandleRepositoryNameFilterCorrectly() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(mockDomainPage);

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("repo", "spring-boot")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Should handle multiple query parameters together")
    void shouldHandleMultipleQueryParametersTogether() throws Exception {
        // Given
        when(repositoryScoringService.scoreRepositories(any(RepositorySearchRequest.class)))
                .thenReturn(mockDomainPage);

        // When & Then
        mockMvc.perform(get("/api/v1/repositories/scores")
                        .param("language", "java")
                        .param("org", "spring-projects")
                        .param("repo", "spring-boot")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    /**
     * Creates a standard mock domain page with two repository scores
     */
    private static com.gerard.githubreposcorer.domain.model.RepositoryScorePage createMockDomainPage() {
        com.gerard.githubreposcorer.domain.model.RepositoryScore domainScore1 =
                com.gerard.githubreposcorer.domain.model.RepositoryScore.builder()
                        .name("spring-projects/spring-boot")
                        .url("https://github.com/spring-projects/spring-boot")
                        .score(new BigDecimal("1.25"))
                        .build();

        com.gerard.githubreposcorer.domain.model.RepositoryScore domainScore2 =
                com.gerard.githubreposcorer.domain.model.RepositoryScore.builder()
                        .name("facebook/react")
                        .url("https://github.com/facebook/react")
                        .score(new BigDecimal("1.15"))
                        .build();

        return com.gerard.githubreposcorer.domain.model.RepositoryScorePage.builder()
                .content(List.of(domainScore1, domainScore2))
                .page(0)
                .size(20)
                .totalElements(2)
                .totalPages(1)
                .build();
    }

    private static com.gerard.githubreposcorer.domain.model.RepositoryScorePage createPaginatedMockDomainPage(int page, int size, int totalElements, int totalPages) {
        com.gerard.githubreposcorer.domain.model.RepositoryScorePage fullPage = createMockDomainPage();

        return com.gerard.githubreposcorer.domain.model.RepositoryScorePage.builder()
                .content(List.of(fullPage.getContent().get(0)))
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
