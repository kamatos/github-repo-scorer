package com.gerard.githubreposcorer.config;

import com.gerard.githubreposcorer.data.RepositoriesSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositorySourcesConfig {

    @Bean
    public RepositoriesSource dummyRepositoriesSource() {
        return request -> null;
    }
}
