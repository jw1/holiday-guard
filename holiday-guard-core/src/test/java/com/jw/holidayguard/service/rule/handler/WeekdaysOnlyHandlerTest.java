package com.jw.holidayguard.service.rule.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RED: Write failing tests for WeekdaysOnlyHandler
 * Testing the specific handler for weekdays-only rule generation
 */
class WeekdaysOnlyHandlerTest {

    private WeekdaysOnlyHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new WeekdaysOnlyHandler();
    }
    
    @Test
    void shouldGenerateWeekdaysForFullWeek() {
        // RED: Test a full week (Mon-Sun) - should only return Mon-Fri
        LocalDate monday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate sunday = LocalDate.of(2025, 1, 12);   // Sunday
        
        List<LocalDate> result = handler.generateDates(null, monday, sunday);
        
        assertEquals(5, result.size());
        assertEquals(LocalDate.of(2025, 1, 6), result.get(0));  // Monday
        assertEquals(LocalDate.of(2025, 1, 7), result.get(1));  // Tuesday  
        assertEquals(LocalDate.of(2025, 1, 8), result.get(2));  // Wednesday
        assertEquals(LocalDate.of(2025, 1, 9), result.get(3));  // Thursday
        assertEquals(LocalDate.of(2025, 1, 10), result.get(4)); // Friday
    }
    
    @Test
    void shouldHandleWeekendOnlyRange() {
        // RED: Test weekend-only range - should return empty list
        LocalDate saturday = LocalDate.of(2025, 1, 4);  // Saturday
        LocalDate sunday = LocalDate.of(2025, 1, 5);    // Sunday
        
        List<LocalDate> result = handler.generateDates(null, saturday, sunday);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void shouldHandleSingleWeekday() {
        // RED: Test single weekday - should return that day
        LocalDate wednesday = LocalDate.of(2025, 1, 8); // Wednesday
        
        List<LocalDate> result = handler.generateDates(null, wednesday, wednesday);
        
        assertEquals(1, result.size());
        assertEquals(wednesday, result.get(0));
    }
    
    @Test
    void shouldHandleSingleWeekendDay() {
        // RED: Test single weekend day - should return empty
        LocalDate saturday = LocalDate.of(2025, 1, 4); // Saturday
        
        List<LocalDate> result = handler.generateDates(null, saturday, saturday);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void shouldHandleMonthBoundary() {
        // RED: Test cross-month boundary with weekdays
        LocalDate endOfJan = LocalDate.of(2025, 1, 30);  // Thursday
        LocalDate startOfFeb = LocalDate.of(2025, 2, 3); // Monday
        
        List<LocalDate> result = handler.generateDates(null, endOfJan, startOfFeb);
        
        assertEquals(3, result.size());
        assertEquals(LocalDate.of(2025, 1, 30), result.get(0)); // Thu Jan 30
        assertEquals(LocalDate.of(2025, 1, 31), result.get(1)); // Fri Jan 31  
        assertEquals(LocalDate.of(2025, 2, 3), result.get(2));  // Mon Feb 3
        // Should skip Sat Feb 1, Sun Feb 2
    }
}