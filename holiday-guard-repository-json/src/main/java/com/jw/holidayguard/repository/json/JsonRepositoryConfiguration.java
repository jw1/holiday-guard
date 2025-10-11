package com.jw.holidayguard.repository.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.IOException;

/**
 * Configuration for JSON file-based repository implementation.
 *
 * <p>This configuration is only active when the "json" profile is enabled.
 * It loads the JSON data file on startup and makes it available as a bean
 * for all repository implementations to use.
 */
@Slf4j
@Configuration
@Profile("json")
@ComponentScan(basePackages = "com.jw.holidayguard.repository.json")
public class JsonRepositoryConfiguration {

    @Value("${app.repo.json.filename:./data.json}")
    private String jsonFilePath;

    /**
     * Loads the JSON data file and creates a JsonDataModel bean.
     *
     * <p>The data is loaded once at startup and kept in memory for the
     * lifetime of the application. All repository queries filter this
     * in-memory data.
     *
     * @return JsonDataModel containing all schedules, versions, rules, and deviations
     * @throws RuntimeException if the JSON file cannot be read or parsed
     */
    @Bean
    public JsonDataModel jsonDataModel() {
        log.info("Loading JSON data from: {}", jsonFilePath);

        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            log.warn("JSON file not found at: {}. Using empty data model.", jsonFilePath);
            return new JsonDataModel();
        }

        try {
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            mapper.registerModule(new JavaTimeModule());

            JsonDataModel data = mapper.readValue(jsonFile, JsonDataModel.class);

            log.info("JSON data loaded successfully:");
            log.info("  - Schedules: {}", data.getSchedules().size());
            log.info("  - Versions: {}", data.getVersions().size());
            log.info("  - Rules: {}", data.getRules().size());
            log.info("  - Deviations: {}", data.getDeviations().size());

            return data;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON data from " + jsonFilePath, e);
        }
    }
}
