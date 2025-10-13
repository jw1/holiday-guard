package com.jw.holidayguard.service.rule.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NoDaysHandler - verifies that all dates return false
 * Useful for schedules that rarely run and use FORCE_RUN deviations
 */
class NoDaysHandlerTest {

    private NoDaysHandler handler;

    @BeforeEach
    void setUp() {
        handler = new NoDaysHandler();
    }

    @Test
    void shouldGenerateEmptyListForFullWeek() {
        // Test a full week (Mon-Sun) - should return all days (but shouldRun will be false)
        LocalDate monday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate sunday = LocalDate.of(2025, 1, 12);   // Sunday

        List<LocalDate> result = handler.generateDates(null, monday, sunday);

        // Note: NoDaysHandler generates the date range but shouldRun returns false
        // This allows the Calendar to still evaluate deviations
        assertEquals(7, result.size());
    }

    @Test
    void shouldGenerateWeekendDays() {
        // Test weekend-only range - should return both days
        LocalDate saturday = LocalDate.of(2025, 1, 4);  // Saturday
        LocalDate sunday = LocalDate.of(2025, 1, 5);    // Sunday

        List<LocalDate> result = handler.generateDates(null, saturday, sunday);

        assertEquals(2, result.size());
    }

    @Test
    void shouldGenerateSingleDay() {
        // Test single day - should return that day
        LocalDate wednesday = LocalDate.of(2025, 1, 8); // Wednesday

        List<LocalDate> result = handler.generateDates(null, wednesday, wednesday);

        assertEquals(1, result.size());
        assertEquals(wednesday, result.get(0));
    }

    @Test
    void shouldRunReturnsFalseForAnyDate() {
        // Test shouldRun always returns false
        LocalDate weekday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate weekend = LocalDate.of(2025, 1, 4);    // Saturday
        LocalDate holiday = LocalDate.of(2025, 12, 25);  // Christmas

        assertFalse(handler.shouldRun(null, weekday));
        assertFalse(handler.shouldRun(null, weekend));
        assertFalse(handler.shouldRun(null, holiday));
    }

    @Test
    void shouldHandleMonthBoundary() {
        // Test cross-month boundary - generates all days but shouldRun is false
        LocalDate endOfJan = LocalDate.of(2025, 1, 30);  // Thursday
        LocalDate startOfFeb = LocalDate.of(2025, 2, 3); // Monday

        List<LocalDate> result = handler.generateDates(null, endOfJan, startOfFeb);

        assertEquals(5, result.size());

        // Verify shouldRun is false for all generated dates
        for (LocalDate date : result) {
            assertFalse(handler.shouldRun(null, date));
        }
    }
}
