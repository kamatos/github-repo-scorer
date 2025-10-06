package com.gerard.githubreposcorer.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryScorePage {
    private List<RepositoryScore> content;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
}