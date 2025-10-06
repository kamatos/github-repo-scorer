package com.gerard.githubreposcorer.scoring.rule;

import com.gerard.githubreposcorer.config.ScoringProperties;
import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import com.gerard.githubreposcorer.scoring.model.ScoringResult;
import com.gerard.githubreposcorer.util.MathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Slf4j
public class FreshnessScoringRule implements ScoringRule {

    private final ScoringProperties.Freshness freshnessConfig;

    @Override
    public void execute(ScoringContext context) {
        try {
            // Calculate freshness score using exponential decay
            BigDecimal score = MathUtils.freshnessFromDays(context.getDaysSinceUpdate(), freshnessConfig.getHalfLifeDays());
            
            ScoringResult result = ScoringResult.success(getName(), score, BigDecimal.valueOf(freshnessConfig.getWeight()));
            context.addResult(result);
        } catch (Exception e) {
            log.error("Error executing freshness scoring rule: {}", e.getMessage(), e);
            ScoringResult result = ScoringResult.failure(getName(), e.getMessage());
            context.addResult(result);
        }
    }

    @Override
    public BigDecimal getWeight() {
        return BigDecimal.valueOf(freshnessConfig.getWeight());
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
