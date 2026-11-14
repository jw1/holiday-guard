package com.jw.holidayguard.controller;

import com.jw.holidayguard.mapper.ScheduleMapperImpl;
import com.jw.holidayguard.repository.DataProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ScheduleMapperImpl.class)
public class ControllerTestConfiguration {

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