package com.jw.holidayguard.cli;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.JacksonException;

import java.io.File;
import java.io.IOException;

/**
 * Loads CLI configuration from JSON files.
 */
public class CLIConfigLoader {

    private final ObjectMapper objectMapper;

    public CLIConfigLoader() {
        this.objectMapper = new JsonMapper();
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
        } catch (JacksonException e) {
            throw new IOException("Failed to parse configuration file: " + configFile.getAbsolutePath() + ". " + e.getMessage(), e);
        }
    }
}
