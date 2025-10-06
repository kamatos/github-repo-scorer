package com.gerard.githubreposcorer.domain.model;

import java.util.List;

public record RepositorySearchResponse(int totalCount, List<GitHubRepository> items) {

}
