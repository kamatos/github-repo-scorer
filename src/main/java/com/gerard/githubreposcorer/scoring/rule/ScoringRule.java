package com.gerard.githubreposcorer.scoring.rule;

import com.gerard.githubreposcorer.scoring.model.ScoringContext;

import java.math.BigDecimal;

public interface ScoringRule {
    
    /**
     * Execute the scoring rule and add the result to the context
     * @param context The scoring context
     */
    void execute(ScoringContext context);
    
    /**
     * Get the weight of this rule for final score calculation
     * @return The rule weight
     */
    BigDecimal getWeight();

    /**
     * Get the name of this rule
     * @return The rule name
     */
    String getName();
}
