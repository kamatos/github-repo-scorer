package com.gerard.githubreposcorer.scoring;

import com.gerard.githubreposcorer.scoring.model.ScoringContext;

import java.math.BigDecimal;

public interface ScoringStrategy {
    
    /**
     * Calculate the score for a repository based on the scoring context
     * @param context The scoring context containing repository metrics
     * @return The calculated score
     */
    BigDecimal calculateScore(ScoringContext context);
    
    /**
     * Get the version of this scoring strategy
     * @return The strategy version
     */
    String getVersion();
}
