package com.jw.holidayguard.service.materialization.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.ScheduleRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RED: Write failing tests for CustomDatesHandler
 * Testing JSON array of specific dates: ["2025-01-15", "2025-02-15", "2025-03-15"]
 */
class CustomDatesHandlerTest {

    private CustomDatesHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new CustomDatesHandler(new ObjectMapper());
    }
    
    @Test
    void shouldGenerateDatesFromJsonArray() {
        // RED: Test valid JSON array of dates
        ScheduleRules customDatesRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CUSTOM_DATES)
            .ruleConfig("[\"2025-01-15\", \"2025-02-15\", \"2025-03-15\"]")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);   // Start of year
        LocalDate toDate = LocalDate.of(2025, 12, 31);   // End of year
        
        List<LocalDate> result = handler.generateDates(customDatesRule, fromDate, toDate);
        
        assertEquals(3, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 15)));
        assertTrue(result.contains(LocalDate.of(2025, 2, 15)));
        assertTrue(result.contains(LocalDate.of(2025, 3, 15)));
    }
    
    @Test
    void shouldFilterDatesWithinRange() {
        // RED: Test that only dates within range are returned
        ScheduleRules customDatesRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CUSTOM_DATES)
            .ruleConfig("[\"2025-01-15\", \"2025-02-15\", \"2025-03-15\"]")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 2, 1);   // Feb start
        LocalDate toDate = LocalDate.of(2025, 2, 28);    // Feb end
        
        List<LocalDate> result = handler.generateDates(customDatesRule, fromDate, toDate);
        
        assertEquals(1, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 2, 15))); // Only Feb 15 is in range
        assertFalse(result.contains(LocalDate.of(2025, 1, 15))); // Jan 15 is before range
        assertFalse(result.contains(LocalDate.of(2025, 3, 15))); // Mar 15 is after range
    }
    
    @Test
    void shouldReturnEmptyForEmptyJsonArray() {
        // RED: Test empty JSON array
        ScheduleRules customDatesRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CUSTOM_DATES)
            .ruleConfig("[]")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 12, 31);
        
        List<LocalDate> result = handler.generateDates(customDatesRule, fromDate, toDate);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void shouldHandleInvalidJsonFormat() {
        // RED: Test invalid JSON format
        ScheduleRules invalidJsonRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CUSTOM_DATES)
            .ruleConfig("not-valid-json")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 12, 31);
        
        assertThrows(IllegalArgumentException.class, () -> {
            handler.generateDates(invalidJsonRule, fromDate, toDate);
        });
    }
    
    @Test
    void shouldHandleInvalidDateFormat() {
        // RED: Test invalid date format in JSON array
        ScheduleRules invalidDateRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CUSTOM_DATES)
            .ruleConfig("[\"2025-13-45\", \"not-a-date\"]")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 12, 31);
        
        assertThrows(IllegalArgumentException.class, () -> {
            handler.generateDates(invalidDateRule, fromDate, toDate);
        });
    }
    
    @Test
    void shouldSortReturnedDates() {
        // RED: Test that returned dates are sorted chronologically
        ScheduleRules customDatesRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CUSTOM_DATES)
            .ruleConfig("[\"2025-03-15\", \"2025-01-15\", \"2025-02-15\"]") // Unsorted input
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 12, 31);
        
        List<LocalDate> result = handler.generateDates(customDatesRule, fromDate, toDate);
        
        assertEquals(3, result.size());
        assertEquals(LocalDate.of(2025, 1, 15), result.get(0)); // Should be sorted
        assertEquals(LocalDate.of(2025, 2, 15), result.get(1));
        assertEquals(LocalDate.of(2025, 3, 15), result.get(2));
    }
}