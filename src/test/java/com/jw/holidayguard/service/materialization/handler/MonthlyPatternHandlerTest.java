package com.jw.holidayguard.service.materialization.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.ScheduleRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RED: Write failing tests for MonthlyPatternHandler
 * Testing JSON patterns like:
 * - {"dayOfMonth": 15, "skipWeekends": true}
 * - {"dayOfWeek": "FRIDAY", "weekOfMonth": "LAST"}
 */
class MonthlyPatternHandlerTest {

    private MonthlyPatternHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new MonthlyPatternHandler(new ObjectMapper());
    }
    
    @Test
    void shouldGenerateSpecificDayOfMonth() {
        // RED: Test 15th of each month
        ScheduleRules monthlyRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.MONTHLY_PATTERN)
            .ruleConfig("{\"dayOfMonth\": 15}")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 3, 31);
        
        List<LocalDate> result = handler.generateDates(monthlyRule, fromDate, toDate);
        
        assertEquals(3, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 15))); // Jan 15
        assertTrue(result.contains(LocalDate.of(2025, 2, 15))); // Feb 15
        assertTrue(result.contains(LocalDate.of(2025, 3, 15))); // Mar 15
    }
    
    @Test
    void shouldSkipWeekendsWhenRequested() {
        // RED: Test 15th of month but skip weekends
        ScheduleRules monthlyRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.MONTHLY_PATTERN)
            .ruleConfig("{\"dayOfMonth\": 15, \"skipWeekends\": true}")
            .build();
            
        // June 2025: 15th is a Sunday, should move to Monday 16th
        LocalDate fromDate = LocalDate.of(2025, 6, 1);
        LocalDate toDate = LocalDate.of(2025, 6, 30);
        
        List<LocalDate> result = handler.generateDates(monthlyRule, fromDate, toDate);
        
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 6, 16), result.get(0)); // Monday after Sunday 15th
    }
    
    @Test
    void shouldGenerateLastDayOfWeekInMonth() {
        // RED: Test last Friday of each month
        ScheduleRules monthlyRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.MONTHLY_PATTERN)
            .ruleConfig("{\"dayOfWeek\": \"FRIDAY\", \"weekOfMonth\": \"LAST\"}")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 2, 28);
        
        List<LocalDate> result = handler.generateDates(monthlyRule, fromDate, toDate);
        
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2025, 1, 31), result.get(0)); // Last Friday of Jan 2025
        assertEquals(LocalDate.of(2025, 2, 28), result.get(1)); // Last Friday of Feb 2025
    }
    
    @Test
    void shouldGenerateFirstDayOfWeekInMonth() {
        // RED: Test first Monday of each month
        ScheduleRules monthlyRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.MONTHLY_PATTERN)
            .ruleConfig("{\"dayOfWeek\": \"MONDAY\", \"weekOfMonth\": \"FIRST\"}")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 2, 28);
        
        List<LocalDate> result = handler.generateDates(monthlyRule, fromDate, toDate);
        
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2025, 1, 6), result.get(0));  // First Monday of Jan 2025
        assertEquals(LocalDate.of(2025, 2, 3), result.get(1));  // First Monday of Feb 2025
    }
    
    @Test
    void shouldHandleEndOfMonth() {
        // RED: Test day 31 but only include months that have 31 days
        ScheduleRules monthlyRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.MONTHLY_PATTERN)
            .ruleConfig("{\"dayOfMonth\": 31}")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 4, 30);
        
        List<LocalDate> result = handler.generateDates(monthlyRule, fromDate, toDate);
        
        assertEquals(2, result.size()); // Only Jan and Mar have 31 days in this range
        assertTrue(result.contains(LocalDate.of(2025, 1, 31))); // Jan 31
        // Feb 31 doesn't exist so shouldn't be in results
        assertTrue(result.contains(LocalDate.of(2025, 3, 31))); // Mar 31
        // Apr 31 doesn't exist so shouldn't be in results
    }
    
    @Test
    void shouldHandleInvalidJsonConfiguration() {
        // RED: Test invalid JSON
        ScheduleRules invalidRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.MONTHLY_PATTERN)
            .ruleConfig("invalid-json")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 31);
        
        assertThrows(IllegalArgumentException.class, () -> {
            handler.generateDates(invalidRule, fromDate, toDate);
        });
    }
}