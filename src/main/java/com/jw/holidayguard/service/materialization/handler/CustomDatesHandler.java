package com.jw.holidayguard.service.materialization.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.ScheduleRules;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * GREEN: Handler for CUSTOM_DATES rule type.
 * Processes JSON array of explicit dates: ["2025-01-15", "2025-02-15", "2025-03-15"]
 */
@Component
public class CustomDatesHandler implements RuleHandler {

    private final ObjectMapper objectMapper;

    public CustomDatesHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LocalDate> generateDates(ScheduleRules rule, LocalDate fromDate, LocalDate toDate) {
        // Handle invalid date range
        if (fromDate.isAfter(toDate)) {
            return Collections.emptyList();
        }
        
        String jsonConfig = rule.getRuleConfig();
        if (jsonConfig == null || jsonConfig.trim().isEmpty()) {
            throw new IllegalArgumentException("Custom dates JSON configuration cannot be null or empty");
        }
        
        try {
            // Parse JSON array of date strings
            List<String> dateStrings = objectMapper.readValue(jsonConfig, new TypeReference<List<String>>() {});
            
            // Convert to LocalDate objects and filter by date range
            return dateStrings.stream()
                .map(this::parseDate)
                .filter(date -> !date.isBefore(fromDate) && !date.isAfter(toDate))
                .sorted()
                .toList();
                
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format or date values in custom dates configuration: " + jsonConfig, e);
        }
    }
    
    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString + ". Expected format: YYYY-MM-DD", e);
        }
    }
    
    @Override
    public ScheduleRules.RuleType getSupportedRuleType() {
        return ScheduleRules.RuleType.CUSTOM_DATES;
    }
}