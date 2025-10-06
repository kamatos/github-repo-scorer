package com.gerard.githubreposcorer.web.controller;

import com.gerard.githubreposcorer.api.RepositoryScoringApi;
import com.gerard.githubreposcorer.api.model.PageRequest;
import com.gerard.githubreposcorer.api.model.RepositoriesScoringRequest;
import com.gerard.githubreposcorer.api.model.RepositoryScorePage;
import com.gerard.githubreposcorer.service.RepositoryScoringService;
import com.gerard.githubreposcorer.web.mapper.RepositoryScoringMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RepositoryScoringController implements RepositoryScoringApi {

    private final RepositoryScoringService repositoryScoringService;
    private final RepositoryScoringMapper repositoryScoringMapper;

    @Override
    public ResponseEntity<RepositoryScorePage> scoreRepositories(RepositoriesScoringRequest request,
                                                                 PageRequest page) {
        var domainRequest = repositoryScoringMapper.toDomainModel(request, page);
        var domainResult = repositoryScoringService.scoreRepositories(domainRequest);
        RepositoryScorePage result = repositoryScoringMapper.toApiModel(domainResult);
        return ResponseEntity.ok(result);
    }

}