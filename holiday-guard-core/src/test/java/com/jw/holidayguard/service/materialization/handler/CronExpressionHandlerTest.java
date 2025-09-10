package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RED: Write failing tests for CronExpressionHandler
 * Testing cron-based rule generation using standard cron expressions
 */
class CronExpressionHandlerTest {

    private CronExpressionHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new CronExpressionHandler();
    }
    
    @Test
    void shouldGenerateWeekdaysFromCronExpression() {
        // RED: Test cron expression "0 0 9 * * MON-FRI" (9 AM weekdays)
        ScheduleRules cronRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CRON_EXPRESSION)
            .ruleConfig("0 0 9 * * MON-FRI")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);  // Wednesday
        LocalDate toDate = LocalDate.of(2025, 1, 7);    // Tuesday
        
        List<LocalDate> result = handler.generateDates(cronRule, fromDate, toDate);
        
        // Should generate same as weekdays: Jan 1 (Wed), Jan 2 (Thu), Jan 3 (Fri), Jan 6 (Mon), Jan 7 (Tue)
        assertEquals(5, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 1))); // Wed
        assertTrue(result.contains(LocalDate.of(2025, 1, 2))); // Thu  
        assertTrue(result.contains(LocalDate.of(2025, 1, 3))); // Fri
        assertFalse(result.contains(LocalDate.of(2025, 1, 4))); // Sat - excluded by cron
        assertFalse(result.contains(LocalDate.of(2025, 1, 5))); // Sun - excluded by cron
        assertTrue(result.contains(LocalDate.of(2025, 1, 6))); // Mon
        assertTrue(result.contains(LocalDate.of(2025, 1, 7))); // Tue
    }
    
    @Test
    void shouldGenerateSpecificDaysFromCronExpression() {
        // RED: Test cron expression "0 0 9 * * TUE,FRI" (9 AM Tuesdays and Fridays)
        ScheduleRules cronRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CRON_EXPRESSION)
            .ruleConfig("0 0 9 * * TUE,FRI")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);  // Wednesday
        LocalDate toDate = LocalDate.of(2025, 1, 7);    // Tuesday
        
        List<LocalDate> result = handler.generateDates(cronRule, fromDate, toDate);
        
        // Should generate only: Jan 3 (Fri), Jan 7 (Tue)
        assertEquals(2, result.size());
        assertFalse(result.contains(LocalDate.of(2025, 1, 1))); // Wed - excluded
        assertTrue(result.contains(LocalDate.of(2025, 1, 3)));  // Fri - included  
        assertTrue(result.contains(LocalDate.of(2025, 1, 7)));  // Tue - included
    }
    
    @Test
    void shouldHandleInvalidCronExpression() {
        // RED: Test invalid cron expression
        ScheduleRules invalidCronRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CRON_EXPRESSION)
            .ruleConfig("invalid-cron")
            .build();
            
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 7);
        
        assertThrows(IllegalArgumentException.class, () -> {
            handler.generateDates(invalidCronRule, fromDate, toDate);
        });
    }
    
    @Test
    void shouldHandleEmptyDateRangeForValidCron() {
        // RED: Test valid cron but no matching dates in range
        ScheduleRules cronRule = ScheduleRules.builder()
            .ruleType(ScheduleRules.RuleType.CRON_EXPRESSION)
            .ruleConfig("0 0 9 * * SUN")  // Only Sundays
            .build();
            
        LocalDate monday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate friday = LocalDate.of(2025, 1, 10);   // Friday (no Sundays in range)
        
        List<LocalDate> result = handler.generateDates(cronRule, monday, friday);
        
        assertTrue(result.isEmpty());
    }
}