package com.gerard.githubreposcorer.scoring.strategy;

import com.gerard.githubreposcorer.config.ScoringConfiguration;
import com.gerard.githubreposcorer.scoring.exception.InvalidWeightsException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ScoringStrategyIntegrationTest {

    @Test
    void shouldFailApplicationStartupIfWeightsAreIncorrect() {
        SpringApplication application = new SpringApplication(DummyConfig.class);

        try (ConfigurableApplicationContext context = application.run("--scoring.stars.weight=1.5", "--scoring.forks.weight=0.5")) {
            fail("Not supposed to reach here");
        } catch (InvalidWeightsException e) {
            assertThat(e.getMessage()).contains("Weights must sum to 1.0");
        } catch (Throwable e) {
            fail("Not supposed to reach here", e);
        }
    }


    @EnableAutoConfiguration
    @TestConfiguration
    @Import({ScoringConfiguration.class, ScoringStrategyV1.class})
    static class DummyConfig {
    }
}
