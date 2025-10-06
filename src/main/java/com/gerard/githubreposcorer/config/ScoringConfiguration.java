package com.gerard.githubreposcorer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(ScoringProperties.class)
public class ScoringConfiguration {
    public static final String SCORING_EXECUTOR_BEAN_NAME = "scoringExecutor";

    @Bean(SCORING_EXECUTOR_BEAN_NAME)
    ExecutorService scoringExecutor() {
        return Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
    }
}
