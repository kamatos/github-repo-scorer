package com.gerard.githubreposcorer.web.mapper;

import com.gerard.githubreposcorer.api.model.PageRequest;
import com.gerard.githubreposcorer.api.model.RepositoriesScoringRequest;
import com.gerard.githubreposcorer.api.model.RepositoryScore;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RepositoryScoringMapper {

    /**
     * Maps domain RepositoryScorePage to API RepositoryScorePage
     */
    public com.gerard.githubreposcorer.api.model.RepositoryScorePage toApiModel(com.gerard.githubreposcorer.domain.model.RepositoryScorePage domainPage) {
        if (domainPage == null) {
            return null;
        }

        com.gerard.githubreposcorer.api.model.RepositoryScorePage apiPage = new com.gerard.githubreposcorer.api.model.RepositoryScorePage();
        apiPage.setPage(domainPage.getPage());
        apiPage.setSize(domainPage.getSize());
        apiPage.setTotalElements(domainPage.getTotalElements());
        apiPage.setTotalPages(domainPage.getTotalPages());

        if (domainPage.getContent() != null) {
            List<RepositoryScore> apiContent = domainPage.getContent().stream()
                    .map(this::toApiModel)
                    .toList();
            apiPage.setContent(apiContent);
        }

        return apiPage;
    }

    /**
     * Maps domain RepositoryScore to API RepositoryScore
     */
    public com.gerard.githubreposcorer.api.model.RepositoryScore toApiModel(com.gerard.githubreposcorer.domain.model.RepositoryScore domainScore) {
        if (domainScore == null) {
            return null;
        }

        com.gerard.githubreposcorer.api.model.RepositoryScore apiScore = new com.gerard.githubreposcorer.api.model.RepositoryScore();
        apiScore.setName(domainScore.getName());
        apiScore.setUrl(java.net.URI.create(domainScore.getUrl()));
        apiScore.setScore(domainScore.getScore().floatValue());

        return apiScore;
    }

    /**
     * Maps API search request and page request to domain search data
     * 
     * @param searchRequest the API search request (can be null)
     * @param pageRequest the API page request (can be null)
     * @return the domain search data with proper defaults
     */
    public RepositorySearchRequest toDomainModel(@Nullable RepositoriesScoringRequest searchRequest,
                                                 @Nullable PageRequest pageRequest) {
        return RepositorySearchRequest.builder()
                .earliestCreationDate(mapEarliestCreationDate(searchRequest))
                .language(mapLanguage(searchRequest))
                .org(mapOrg(searchRequest))
                .repo(mapRepo(searchRequest))
                .page(mapPage(pageRequest))
                .size(mapSize(pageRequest))
                .build();
    }

    private LocalDateTime mapEarliestCreationDate(@Nullable RepositoriesScoringRequest searchRequest) {
        if (searchRequest == null || searchRequest.getEarliestCreationDate() == null) {
            return null;
        }
        return searchRequest.getEarliestCreationDate().toLocalDateTime();
    }

    private String mapLanguage(@Nullable RepositoriesScoringRequest searchRequest) {
        return searchRequest != null ? searchRequest.getLanguage() : null;
    }

    private String mapOrg(@Nullable RepositoriesScoringRequest searchRequest) {
        return searchRequest != null ? searchRequest.getOrg() : null;
    }

    private String mapRepo(@Nullable RepositoriesScoringRequest searchRequest) {
        return searchRequest != null ? searchRequest.getRepo() : null;
    }

    private int mapPage(@Nullable PageRequest pageRequest) {
        return pageRequest != null && pageRequest.getPage() != null ? pageRequest.getPage() : 0;
    }

    private int mapSize(@Nullable PageRequest pageRequest) {
        return pageRequest != null && pageRequest.getSize() != null ? pageRequest.getSize() : 20;
    }
}
