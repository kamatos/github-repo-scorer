package com.gerard.githubreposcorer.config;

import com.gerard.githubreposcorer.data.GitHubApiClient;
import com.gerard.githubreposcorer.data.RepositoriesSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RepositorySourcesConfig {

    /**
     * Registers the GitHub API client as the primary repository source implementation.
     *
     * @return the repositories sources implementation
     */
    @Bean
    public RepositoriesSource githubRepositoriesSource(RestClient restClient, GitHubApiProperties gitHubApiProperties) {
        return new GitHubApiClient(restClient, gitHubApiProperties);
    }
}
