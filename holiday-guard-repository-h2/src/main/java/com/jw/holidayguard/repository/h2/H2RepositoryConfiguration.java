package com.jw.holidayguard.repository.h2;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration for H2/JPA repository implementation.
 *
 * <p>This configuration is only active when the "h2" profile is enabled.
 * It enables JPA repositories and entity scanning for the Holiday Guard domain model.
 *
 * <p>The @EnableJpaRepositories annotation tells Spring Data JPA to scan for
 * repository interfaces in the parent com.jw.holidayguard.repository package.
 */
@Configuration
@Profile("h2")
@EnableJpaRepositories(basePackages = "com.jw.holidayguard.repository")
@EntityScan(basePackages = "com.jw.holidayguard.domain")
@ComponentScan(basePackages = "com.jw.holidayguard.repository.h2")
public class H2RepositoryConfiguration {
    // Configuration only - Spring Data JPA will auto-implement repository interfaces
}
