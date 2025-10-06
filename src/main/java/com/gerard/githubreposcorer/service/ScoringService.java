package com.gerard.githubreposcorer.service;

import com.gerard.githubreposcorer.scoring.ScoringStrategy;
import com.gerard.githubreposcorer.scoring.ScoringStrategyFactory;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringService {

    private final ScoringStrategyFactory scoringStrategyFactory;

    public BigDecimal calculateScore(ScoringContext context) {
        try {
            ScoringStrategy strategy = scoringStrategyFactory.createStrategy();
            BigDecimal score = strategy.calculateScore(context);
            
            return score.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error calculating score: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
}