package com.jw.holidayguard.controller;

import com.jw.holidayguard.repository.DataProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ControllerTestConfiguration {

    /**
     * Static initialization to ensure DataProvider is available before condition evaluation.
     * This bean must be created as early as possible in the context lifecycle.
     */
    static {
        System.setProperty("holiday-guard.test.management-enabled", "true");
    }

    /**
     * Provides a test DataProvider that supports management operations.
     * This allows @ConditionalOnManagement controllers to be registered in tests.
     */
    @Bean
    public static DataProvider testDataProvider() {
        return new DataProvider() {
            @Override
            public String getProviderName() {
                return "Test";
            }

            @Override
            public String getStorageDescription() {
                return "In-memory test provider";
            }

            @Override
            public boolean supportsManagement() {
                return true;  // Enable management controllers in tests
            }
        };
    }
}