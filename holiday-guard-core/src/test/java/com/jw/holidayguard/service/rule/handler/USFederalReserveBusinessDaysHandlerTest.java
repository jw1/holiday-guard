package com.jw.holidayguard.service.rule.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for USFederalReserveBusinessDaysHandler
 * Verifies weekday filtering and federal holiday exclusion
 */
class USFederalReserveBusinessDaysHandlerTest {

    private USFederalReserveBusinessDaysHandler handler;

    @BeforeEach
    void setUp() {
        handler = new USFederalReserveBusinessDaysHandler();
    }

    @Test
    void shouldGenerateWeekdaysForNormalWeek() {
        // Test a normal week without holidays - should return Mon-Fri
        LocalDate monday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate friday = LocalDate.of(2025, 1, 10);   // Friday

        List<LocalDate> result = handler.generateDates(null, monday, friday);

        assertEquals(5, result.size());
        assertEquals(LocalDate.of(2025, 1, 6), result.get(0));   // Monday
        assertEquals(LocalDate.of(2025, 1, 7), result.get(1));   // Tuesday
        assertEquals(LocalDate.of(2025, 1, 8), result.get(2));   // Wednesday
        assertEquals(LocalDate.of(2025, 1, 9), result.get(3));   // Thursday
        assertEquals(LocalDate.of(2025, 1, 10), result.get(4));  // Friday
    }

    @Test
    void shouldExcludeWeekends() {
        // Test that weekends are excluded
        LocalDate saturday = LocalDate.of(2025, 1, 4);  // Saturday
        LocalDate sunday = LocalDate.of(2025, 1, 5);    // Sunday

        List<LocalDate> result = handler.generateDates(null, saturday, sunday);

        assertTrue(result.isEmpty(), "Weekends should not be business days");
    }

    @Test
    void shouldExcludeNewYearsDay() {
        // Test that New Year's Day is excluded
        LocalDate newYears = LocalDate.of(2025, 1, 1); // Wednesday, New Year's Day

        assertFalse(handler.shouldRun(null, newYears), "New Year's Day should not be a business day");
    }

    @Test
    void shouldExcludeIndependenceDay() {
        // Test that Independence Day is excluded
        LocalDate july4 = LocalDate.of(2025, 7, 4); // Friday, Independence Day

        assertFalse(handler.shouldRun(null, july4), "Independence Day should not be a business day");
    }

    @Test
    void shouldExcludeChristmas() {
        // Test that Christmas is excluded
        LocalDate christmas = LocalDate.of(2025, 12, 25); // Thursday, Christmas

        assertFalse(handler.shouldRun(null, christmas), "Christmas should not be a business day");
    }

    @Test
    void shouldExcludeThanksgiving() {
        // Test that Thanksgiving (4th Thursday in November) is excluded
        LocalDate thanksgiving = LocalDate.of(2025, 11, 27); // 4th Thursday in November 2025

        assertFalse(handler.shouldRun(null, thanksgiving), "Thanksgiving should not be a business day");
    }

    @Test
    void shouldExcludeMemorialDay() {
        // Test that Memorial Day (last Monday in May) is excluded
        LocalDate memorialDay = LocalDate.of(2025, 5, 26); // Last Monday in May 2025

        assertFalse(handler.shouldRun(null, memorialDay), "Memorial Day should not be a business day");
    }

    @Test
    void shouldExcludeLaborDay() {
        // Test that Labor Day (first Monday in September) is excluded
        LocalDate laborDay = LocalDate.of(2025, 9, 1); // First Monday in September 2025

        assertFalse(handler.shouldRun(null, laborDay), "Labor Day should not be a business day");
    }

    @Test
    void shouldExcludeJuneteenth() {
        // Test that Juneteenth (June 19, federal holiday since 2021) is excluded
        LocalDate juneteenth = LocalDate.of(2025, 6, 19); // Thursday, June 19

        assertFalse(handler.shouldRun(null, juneteenth), "Juneteenth should not be a business day");
    }

    @Test
    void shouldIncludeRegularWeekday() {
        // Test that a regular weekday (not a holiday) is included
        LocalDate regularWeekday = LocalDate.of(2025, 3, 12); // Wednesday, no holiday

        assertTrue(handler.shouldRun(null, regularWeekday), "Regular weekdays should be business days");
    }

    @Test
    void shouldHandleWeekWithHoliday() {
        // Test a week that includes a holiday (July 4th week)
        LocalDate monday = LocalDate.of(2025, 6, 30);   // Monday before July 4th
        LocalDate friday = LocalDate.of(2025, 7, 4);    // Friday, July 4th

        List<LocalDate> result = handler.generateDates(null, monday, friday);

        // Should include Mon-Thu (Jun 30, Jul 1, 2, 3) but not Fri (Jul 4 - holiday)
        assertEquals(4, result.size());
        assertFalse(result.contains(LocalDate.of(2025, 7, 4)), "July 4th should be excluded");
    }

    @Test
    void shouldHandleYearBoundary() {
        // Test year boundary with New Year's Day
        LocalDate dec30 = LocalDate.of(2024, 12, 30); // Monday
        LocalDate jan3 = LocalDate.of(2025, 1, 3);    // Friday

        List<LocalDate> result = handler.generateDates(null, dec30, jan3);

        // Should include: Mon 12/30, Tue 12/31, Thu 1/2, Fri 1/3
        // Should exclude: Sat 12/31, Sun 1/1 (weekend), Wed 1/1 (New Year's)
        // Note: 2024-12-31 is Tuesday, 2025-01-01 is Wednesday (New Year's)
        assertEquals(4, result.size());
        assertFalse(result.contains(LocalDate.of(2025, 1, 1)), "New Year's Day should be excluded");
    }

    @Test
    void shouldIncludeFridayBeforeMemorialDayWeekend() {
        // Test that the Friday before Memorial Day weekend is a business day
        LocalDate friday = LocalDate.of(2025, 5, 23); // Friday before Memorial Day

        assertTrue(handler.shouldRun(null, friday), "Friday before Memorial Day should be a business day");
    }

    @Test
    void shouldExcludeVeteransDay() {
        // Test that Veterans Day (November 11) is excluded
        LocalDate veteransDay = LocalDate.of(2025, 11, 11); // Tuesday, Veterans Day

        assertFalse(handler.shouldRun(null, veteransDay), "Veterans Day should not be a business day");
    }

    @Test
    void shouldExcludeMartinLutherKingJrDay() {
        // Test that MLK Jr Day (3rd Monday in January) is excluded
        LocalDate mlkDay = LocalDate.of(2025, 1, 20); // 3rd Monday in January 2025

        assertFalse(handler.shouldRun(null, mlkDay), "MLK Jr Day should not be a business day");
    }

    @Test
    void shouldExcludePresidentsDay() {
        // Test that Presidents' Day (3rd Monday in February) is excluded
        LocalDate presidentsDay = LocalDate.of(2025, 2, 17); // 3rd Monday in February 2025

        assertFalse(handler.shouldRun(null, presidentsDay), "Presidents' Day should not be a business day");
    }

    @Test
    void shouldExcludeColumbusDay() {
        // Test that Columbus Day (2nd Monday in October) is excluded
        LocalDate columbusDay = LocalDate.of(2025, 10, 13); // 2nd Monday in October 2025

        assertFalse(handler.shouldRun(null, columbusDay), "Columbus Day should not be a business day");
    }
}
