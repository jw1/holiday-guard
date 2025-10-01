package com.jw.holidayguard.util;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.dto.request.CreateDeviationRequest;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ACHProcessingScheduleFactory to ensure proper Federal Reserve business day logic.
 */
class ACHProcessingScheduleFactoryTest {

    @Test
    void shouldCreateACHScheduleDefinition() {
        ACHProcessingScheduleFactory.ACHScheduleDefinition definition =
                ACHProcessingScheduleFactory.createACHSchedule(2025);

        assertNotNull(definition);
        assertNotNull(definition.getSchedule());
        assertEquals("ACH Processing", definition.getSchedule().getName());
        assertEquals("ACH file processing on Federal Reserve business days (weekdays excluding federal holidays)",
                definition.getSchedule().getDescription());
        assertEquals("US", definition.getSchedule().getCountry());
        assertTrue(definition.getSchedule().isActive());

        assertNotNull(definition.getRule());
        assertEquals(Rule.RuleType.WEEKDAYS_ONLY, definition.getRule().getRuleType());
        assertNull(definition.getRule().getRuleConfig());
        assertEquals(LocalDate.of(2025, 1, 1), definition.getRule().getEffectiveFrom());
        assertTrue(definition.getRule().isActive());

        assertNotNull(definition.getHolidayOverrides());
        assertEquals(11, definition.getHolidayOverrides().size()); // All 2025 federal holidays
    }

    @Test
    void shouldCreateSameDayACHScheduleDefinition() {
        ACHProcessingScheduleFactory.ACHScheduleDefinition definition =
                ACHProcessingScheduleFactory.createSameDayACHSchedule(2025);

        assertNotNull(definition);
        assertEquals("Same-Day ACH Processing", definition.getSchedule().getName());
        assertEquals("Same-day ACH processing on Federal Reserve business days",
                definition.getSchedule().getDescription());
        assertTrue(definition.getSchedule().isActive());

        assertEquals(Rule.RuleType.WEEKDAYS_ONLY, definition.getRule().getRuleType());
        assertEquals(11, definition.getHolidayOverrides().size());
    }

    @Test
    void shouldGenerateFederalHolidaysFor2025() {
        List<LocalDate> holidays = ACHProcessingScheduleFactory.USFederalHolidays.getHolidays(2025);

        // Should have all 11 federal holidays (including Juneteenth since 2021)
        assertEquals(11, holidays.size());

        // Check some specific holidays for 2025
        assertTrue(holidays.contains(LocalDate.of(2025, 1, 1)));  // New Year's Day
        assertTrue(holidays.contains(LocalDate.of(2025, 7, 4)));  // Independence Day
        assertTrue(holidays.contains(LocalDate.of(2025, 12, 25))); // Christmas
        assertTrue(holidays.contains(LocalDate.of(2025, 6, 19))); // Juneteenth

        // Check floating holidays for 2025
        assertTrue(holidays.contains(LocalDate.of(2025, 1, 20))); // MLK Day (3rd Monday of Jan)
        assertTrue(holidays.contains(LocalDate.of(2025, 2, 17))); // Presidents Day (3rd Monday of Feb)
        assertTrue(holidays.contains(LocalDate.of(2025, 5, 26))); // Memorial Day (last Monday of May)
        assertTrue(holidays.contains(LocalDate.of(2025, 9, 1)));  // Labor Day (1st Monday of Sept)
        assertTrue(holidays.contains(LocalDate.of(2025, 10, 13))); // Columbus Day (2nd Monday of Oct)
        assertTrue(holidays.contains(LocalDate.of(2025, 11, 11))); // Veterans Day
        assertTrue(holidays.contains(LocalDate.of(2025, 11, 27))); // Thanksgiving (4th Thursday of Nov)
    }

    @Test
    void shouldIncludeJuneteenthStarting2021() {
        List<LocalDate> holidays2020 = ACHProcessingScheduleFactory.USFederalHolidays.getHolidays(2020);
        List<LocalDate> holidays2021 = ACHProcessingScheduleFactory.USFederalHolidays.getHolidays(2021);

        // 2020 should have 10 holidays (no Juneteenth)
        assertEquals(10, holidays2020.size());
        assertFalse(holidays2020.contains(LocalDate.of(2020, 6, 19)));

        // 2021 should have 11 holidays (includes Juneteenth)
        assertEquals(11, holidays2021.size());
        assertTrue(holidays2021.contains(LocalDate.of(2021, 6, 19)));
    }

    @Test
    void shouldCreateFederalHolidaySkipOverrides() {
        List<CreateDeviationRequest> overrides =
                ACHProcessingScheduleFactory.USFederalHolidays.createSkipOverrides(2025);

        assertEquals(11, overrides.size());

        // Check that all are SKIP overrides
        overrides.forEach(override -> {
            assertEquals(Deviation.Action.SKIP, override.getAction());
            assertEquals("system", override.getCreatedBy());
            assertNull(override.getExpiresAt()); // Permanent holidays
            assertTrue(override.getReason().startsWith("Federal Holiday:"));
        });
    }

    @Test
    void shouldIncludeActualHolidayDates() {
        // Test that we get the actual holiday dates, even if they fall on weekends
        List<LocalDate> holidays2024 = ACHProcessingScheduleFactory.USFederalHolidays.getHolidays(2024);

        // Should include actual dates - Christmas 2024 is Wednesday, New Year's 2024 is Monday
        assertTrue(holidays2024.contains(LocalDate.of(2024, 1, 1)));  // New Year's Day 2024
        assertTrue(holidays2024.contains(LocalDate.of(2024, 12, 25))); // Christmas 2024

        // Should have correct count
        assertEquals(11, holidays2024.size()); // All federal holidays including Juneteenth
    }

    @Test
    void shouldCreateUpdateRequestForRestAPI() {
        ACHProcessingScheduleFactory.ACHScheduleDefinition definition =
                ACHProcessingScheduleFactory.createACHSchedule(2025);

        UpdateRuleRequest updateRequest = definition.toUpdateRequest();

        assertNotNull(updateRequest);
        assertNotNull(updateRequest.getEffectiveFrom());
        assertNotNull(updateRequest.getRule());
        assertEquals(11, updateRequest.getDeviations().size());

        // Verify rule details
        CreateRuleRequest rule = updateRequest.getRule();
        assertEquals(Rule.RuleType.WEEKDAYS_ONLY, rule.getRuleType());
        assertNull(rule.getRuleConfig());
        assertTrue(rule.isActive());

        // Verify deviation details
        updateRequest.getDeviations().forEach(deviation -> {
            assertEquals(Deviation.Action.SKIP, deviation.getAction());
            assertEquals("system", deviation.getCreatedBy());
            assertTrue(deviation.getReason().startsWith("Federal Holiday:"));
        });
    }

    @Test
    void shouldBeConsistentAcrossYears() {
        // Test that holiday calculation is consistent and correct for different years
        for (int year = 2020; year <= 2030; year++) {
            List<LocalDate> holidays = ACHProcessingScheduleFactory.USFederalHolidays.getHolidays(year);

            // Should have correct number of holidays
            int expectedCount = (year >= 2021) ? 11 : 10; // Juneteenth added in 2021
            assertEquals(expectedCount, holidays.size(), "Wrong number of holidays for year " + year);

            // All holidays should be sorted
            for (int i = 1; i < holidays.size(); i++) {
                assertTrue(holidays.get(i).isAfter(holidays.get(i - 1)),
                        "Holidays should be sorted chronologically for year " + year);
            }

            // All holidays should be in the correct year
            final int finalYear = year; // Make effectively final for lambda
            holidays.forEach(holiday ->
                    assertEquals(finalYear, holiday.getYear(), "Holiday should be in year " + finalYear)
            );
        }
    }
}
