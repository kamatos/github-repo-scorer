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
public class RepositorySearchRequest {
    private LocalDateTime earliestCreationDate;
    private String language;
    private String org;
    private String repo;
    private int page;
    private int size;
}