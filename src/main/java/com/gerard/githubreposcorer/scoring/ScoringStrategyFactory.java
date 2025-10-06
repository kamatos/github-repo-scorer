package com.gerard.githubreposcorer.scoring;

import com.gerard.githubreposcorer.config.ScoringProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScoringStrategyFactory {

    private final ScoringProperties scoringProperties;
    private final Map<String, ScoringStrategy> scoringStrategies;

    public ScoringStrategyFactory(ScoringProperties scoringProperties,
                                  List<ScoringStrategy> scoringStrategies) {
        this.scoringProperties = scoringProperties;
        this.scoringStrategies = scoringStrategies.stream().collect(
                Collectors.toMap(ScoringStrategy::getVersion, strategy -> strategy)
        );
    }

    public ScoringStrategy createStrategy() {
        return createStrategy(Optional.empty());
    }

    public ScoringStrategy createStrategy(Optional<String> strategyName) {
        String defaultStrategyName = scoringProperties.getStrategy().getVersion().toLowerCase();
        String effectiveStrategyName = strategyName.orElse(defaultStrategyName);

        var strategy = Optional.ofNullable(scoringStrategies.get(effectiveStrategyName.toLowerCase()))
                .orElseGet(() -> scoringStrategies.get(defaultStrategyName));
        return Optional.ofNullable(strategy)
                .orElseThrow(() -> new IllegalStateException("No scoring strategy found for name: " + effectiveStrategyName));
    }
}
