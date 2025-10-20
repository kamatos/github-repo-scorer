package com.gerard.githubreposcorer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github.api")
public class GitHubApiProperties {
    private String token = "";
    private String baseUrl = "https://api.github.com";
}