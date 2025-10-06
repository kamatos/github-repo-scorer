package com.gerard.githubreposcorer.scoring.rule;

import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import com.gerard.githubreposcorer.scoring.model.ScoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Slf4j
public class CompositeScoringRule implements ScoringRule {

    private final List<ScoringRule> rules;
    private final ExecutorService executorService;

    @Override
    public void execute(ScoringContext context) {
        try {
            // Execute all rules in parallel
            List<CompletableFuture<Void>> futures = rules.stream()
                    .map(rule -> CompletableFuture.runAsync(() -> {
                        try {
                            rule.execute(context);
                        } catch (Exception e) {
                            log.error("Error executing rule {}: {}", rule.getName(), e.getMessage(), e);
                            ScoringResult result = ScoringResult.failure(rule.getName(), e.getMessage());
                            context.addResult(result);
                        }
                    }, executorService))
                    .toList();

            // Wait for all rules to complete
            futures.forEach(CompletableFuture::join);

        } catch (Exception e) {
            log.error("Error executing parallel scoring rule {}: {}", getName(), e.getMessage(), e);
            ScoringResult result = ScoringResult.failure(getName(), e.getMessage());
            context.addResult(result);
        }
    }

    @Override
    public BigDecimal getWeight() {
        return BigDecimal.ZERO; // Composite rule has weight 0, individual rules handle their own weights
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
