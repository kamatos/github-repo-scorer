package com.gerard.githubreposcorer.service;

import com.gerard.githubreposcorer.domain.model.RepositoryScorePage;
import com.gerard.githubreposcorer.domain.model.RepositorySearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryScoringService {

    public RepositoryScorePage scoreRepositories(RepositorySearchRequest request) {
        return null;
    }
}