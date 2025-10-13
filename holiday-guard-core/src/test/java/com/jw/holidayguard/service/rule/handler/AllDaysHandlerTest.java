package com.jw.holidayguard.service.rule.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AllDaysHandler - verifies that all dates return true
 */
class AllDaysHandlerTest {

    private AllDaysHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AllDaysHandler();
    }

    @Test
    void shouldGenerateAllDaysForFullWeek() {
        // Test a full week (Mon-Sun) - should return all 7 days
        LocalDate monday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate sunday = LocalDate.of(2025, 1, 12);   // Sunday

        List<LocalDate> result = handler.generateDates(null, monday, sunday);

        assertEquals(7, result.size());
        assertEquals(LocalDate.of(2025, 1, 6), result.get(0));   // Monday
        assertEquals(LocalDate.of(2025, 1, 7), result.get(1));   // Tuesday
        assertEquals(LocalDate.of(2025, 1, 8), result.get(2));   // Wednesday
        assertEquals(LocalDate.of(2025, 1, 9), result.get(3));   // Thursday
        assertEquals(LocalDate.of(2025, 1, 10), result.get(4));  // Friday
        assertEquals(LocalDate.of(2025, 1, 11), result.get(5));  // Saturday
        assertEquals(LocalDate.of(2025, 1, 12), result.get(6));  // Sunday
    }

    @Test
    void shouldIncludeWeekendDays() {
        // Test weekend-only range - should return both days
        LocalDate saturday = LocalDate.of(2025, 1, 4);  // Saturday
        LocalDate sunday = LocalDate.of(2025, 1, 5);    // Sunday

        List<LocalDate> result = handler.generateDates(null, saturday, sunday);

        assertEquals(2, result.size());
        assertEquals(saturday, result.get(0));
        assertEquals(sunday, result.get(1));
    }

    @Test
    void shouldHandleSingleDay() {
        // Test single day - should return that day
        LocalDate wednesday = LocalDate.of(2025, 1, 8); // Wednesday

        List<LocalDate> result = handler.generateDates(null, wednesday, wednesday);

        assertEquals(1, result.size());
        assertEquals(wednesday, result.get(0));
    }

    @Test
    void shouldHandleMonthBoundary() {
        // Test cross-month boundary - should include all days
        LocalDate endOfJan = LocalDate.of(2025, 1, 30);  // Thursday
        LocalDate startOfFeb = LocalDate.of(2025, 2, 3); // Monday

        List<LocalDate> result = handler.generateDates(null, endOfJan, startOfFeb);

        assertEquals(5, result.size());
        assertEquals(LocalDate.of(2025, 1, 30), result.get(0)); // Thu Jan 30
        assertEquals(LocalDate.of(2025, 1, 31), result.get(1)); // Fri Jan 31
        assertEquals(LocalDate.of(2025, 2, 1), result.get(2));  // Sat Feb 1
        assertEquals(LocalDate.of(2025, 2, 2), result.get(3));  // Sun Feb 2
        assertEquals(LocalDate.of(2025, 2, 3), result.get(4));  // Mon Feb 3
    }

    @Test
    void shouldRunReturnsTrueForAnyDate() {
        // Test shouldRun always returns true
        LocalDate weekday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate weekend = LocalDate.of(2025, 1, 4);    // Saturday
        LocalDate holiday = LocalDate.of(2025, 12, 25);  // Christmas

        assertTrue(handler.shouldRun(null, weekday));
        assertTrue(handler.shouldRun(null, weekend));
        assertTrue(handler.shouldRun(null, holiday));
    }
}
