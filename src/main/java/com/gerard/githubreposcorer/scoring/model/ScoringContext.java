package com.gerard.githubreposcorer.scoring.model;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Builder
@Getter
public class ScoringContext {
    private int stars;
    private int forks;
    private final ConcurrentMap<String, ScoringResult> results = new ConcurrentHashMap<>();

    public void addResult(ScoringResult result) {
        this.results.put(result.ruleName(), result);
    }

    public List<ScoringResult> getResults() {
        return new ArrayList<>(this.results.values());
    }
}