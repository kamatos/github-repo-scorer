package com.gerard.githubreposcorer.data;

import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import com.gerard.githubreposcorer.domain.model.RepositorySearchResponse;

/**
 * Interface for repository data sources.
 * This allows for pluggable implementations of different repository providers.
 */
public interface RepositoriesSource {
    
    /**
     * Search for repositories based on the given request criteria.
     *
     * @param request the search criteria
     * @return the search response containing repositories and metadata
     */
    RepositorySearchResponse searchRepositories(RepositorySearchRequest request);
}
