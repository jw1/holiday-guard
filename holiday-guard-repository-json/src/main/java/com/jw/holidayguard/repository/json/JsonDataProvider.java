package com.jw.holidayguard.repository.json;

import com.jw.holidayguard.repository.DataProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * JSON file-based implementation of the DataProvider marker interface.
 *
 * <p>This bean is only active when the "json" profile is enabled.
 * It signals to the application that the JSON file-based repository implementation
 * is being used for data persistence (read-only).
 */
@Slf4j
@Component
@Profile("json")
public class JsonDataProvider implements DataProvider {

    @Value("${app.repo.json.filename:./data.json}")
    private String jsonFilePath;

    @PostConstruct
    public void logActivation() {
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  JSON Repository Implementation ACTIVE                 ║");
        log.info("║  File: {}  ", jsonFilePath);
        log.info("║  Mode: READ-ONLY (no CRUD operations)                  ║");
        log.info("╚════════════════════════════════════════════════════════╝");
    }

    @Override
    public String getProviderName() {
        return "JSON";
    }

    @Override
    public String getStorageDescription() {
        return "JSON file at " + jsonFilePath + " (read-only)";
    }

    @Override
    public boolean supportsManagement() {
        return false;  // JSON repository is read-only
    }
}
