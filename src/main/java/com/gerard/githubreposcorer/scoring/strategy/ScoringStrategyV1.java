package com.gerard.githubreposcorer.scoring.strategy;

import com.gerard.githubreposcorer.config.ScoringConfiguration;
import com.gerard.githubreposcorer.config.ScoringProperties;
import com.gerard.githubreposcorer.scoring.ScoringStrategy;
import com.gerard.githubreposcorer.scoring.exception.InvalidWeightsException;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import com.gerard.githubreposcorer.scoring.model.ScoringResult;
import com.gerard.githubreposcorer.scoring.rule.CompositeScoringRule;
import com.gerard.githubreposcorer.scoring.rule.ForksScoringRule;
import com.gerard.githubreposcorer.scoring.rule.FreshnessScoringRule;
import com.gerard.githubreposcorer.scoring.rule.ScoringRule;
import com.gerard.githubreposcorer.scoring.rule.StarsScoringRule;
import com.gerard.githubreposcorer.util.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class ScoringStrategyV1 implements ScoringStrategy, SmartInitializingSingleton {

    private final ScoringProperties scoringProperties;

    private final ExecutorService executorService;

    private ScoringRule ruleChain;

    public ScoringStrategyV1(ScoringProperties scoringProperties,
                             @Qualifier(ScoringConfiguration.SCORING_EXECUTOR_BEAN_NAME) ExecutorService executorService) {
        this.scoringProperties = scoringProperties;
        this.executorService = executorService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        StarsScoringRule starsRule = new StarsScoringRule(scoringProperties.getStars());
        ForksScoringRule forksRule = new ForksScoringRule(scoringProperties.getForks());
        FreshnessScoringRule freshnessRule = new FreshnessScoringRule(scoringProperties.getFreshness());

        validateWeights(List.of(starsRule, forksRule, freshnessRule));

        ruleChain = new CompositeScoringRule(
                List.of(starsRule, forksRule, freshnessRule),
                executorService
        );
    }

    @Override
    public BigDecimal calculateScore(ScoringContext context) {
        ruleChain.execute(context);

        return calculateFinalScore(context.getResults());
    }

    private void validateWeights(List<ScoringRule> rules) {
        BigDecimal totalWeight = rules.stream()
                .map(ScoringRule::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(BigDecimal.ONE) != 0) {
            throw new InvalidWeightsException(
                    String.format("Weights must sum to 1.0, but sum is: %s", totalWeight)
            );
        }

        // Check if any weight exceeds 1.0
        boolean hasExcessiveWeight = rules.stream()
                .anyMatch(rule -> rule.getWeight().compareTo(BigDecimal.ONE) > 0);

        if (hasExcessiveWeight) {
            throw new InvalidWeightsException("No individual weight can exceed 1.0");
        }
    }

    private BigDecimal calculateFinalScore(List<ScoringResult> results) {
        return results.stream()
                .filter(ScoringResult::success)
                .map(result -> result.score().multiply(result.weight()))
                .reduce(BigDecimal.ZERO, (bd1, bd2) -> bd1.add(bd2, MathUtils.MATH_CONTEXT));
    }

    @Override
    public String getVersion() {
        return "v1";
    }
}
