package com.jw.holidayguard.repository;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.jw.holidayguard.domain")
@EnableJpaRepositories("com.jw.holidayguard.repository")
public class RepositoryTestConfiguration {
    // Test configuration for repository tests
}