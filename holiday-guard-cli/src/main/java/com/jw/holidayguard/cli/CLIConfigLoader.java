package com.jw.holidayguard.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;

/**
 * Loads CLI configuration from JSON files.
 */
public class CLIConfigLoader {

    private final ObjectMapper objectMapper;

    public CLIConfigLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Load configuration from a JSON file.
     *
     * @param configFile the JSON configuration file
     * @return parsed configuration
     * @throws IOException if file cannot be read or parsed
     */
    public CLIConfig loadConfig(File configFile) throws IOException {
        try {
            return objectMapper.readValue(configFile, CLIConfig.class);
        } catch (IOException e) {
            throw new IOException("Failed to parse configuration file: " + configFile.getAbsolutePath() + ". " + e.getMessage(), e);
        }
    }
}
