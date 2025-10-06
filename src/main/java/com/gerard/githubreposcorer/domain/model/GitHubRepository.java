package com.gerard.githubreposcorer.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {
    private String name;
    private String fullName;
    private String htmlUrl;
    private String language;
    private int stars;
    private int forks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String owner;
}