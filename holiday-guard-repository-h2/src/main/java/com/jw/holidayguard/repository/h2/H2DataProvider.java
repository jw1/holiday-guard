package com.jw.holidayguard.repository.h2;

import com.jw.holidayguard.repository.DataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * H2/JPA implementation of the DataProvider marker interface.
 *
 * <p>This bean is only active when the "h2" profile is enabled.
 * It signals to the application that the H2 database repository implementation
 * is being used for data persistence.
 */
@Slf4j
@Component
@Profile("h2")
public class H2DataProvider implements DataProvider {

    @Value("${spring.datasource.url:jdbc:h2:file:./holiday-guard-data}")
    private String datasourceUrl;

    @PostConstruct
    public void logActivation() {
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  H2 Repository Implementation ACTIVE                   ║");
        log.info("║  Database: {}  ", datasourceUrl);
        log.info("╚════════════════════════════════════════════════════════╝");
    }

    @Override
    public String getProviderName() {
        return "H2";
    }

    @Override
    public String getStorageDescription() {
        return "H2 SQL Database at " + datasourceUrl;
    }

    @Override
    public boolean supportsManagement() {
        return true;  // H2 supports full CRUD operations
    }
}
