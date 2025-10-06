package com.gerard.githubreposcorer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "scoring")
public class ScoringProperties {

    private Strategy strategy = new Strategy();
    private Stars stars = new Stars();
    private Forks forks = new Forks();

    @Data
    public static class Strategy {
        private String version = "v1";
    }

    @Data
    public static class Stars {
        private int cap = 10000;
        private double weight = 0.5;
    }

    @Data
    public static class Forks {
        private int cap = 10000;
        private double weight = 0.5;
    }
}
