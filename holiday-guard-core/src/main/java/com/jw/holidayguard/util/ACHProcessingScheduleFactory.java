package com.jw.holidayguard.util;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.dto.request.CreateDeviationRequest;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating ACH processing schedule definitions that integrate with Holiday Guard.
 *
 * Generates a complete ACH schedule as a combination of:
 * - Base rule: WEEKDAYS_ONLY (Monday-Friday)
 * - Skip overrides: All US Federal holidays
 *
 * This produces the standard banking "Federal Reserve business days" calendar.
 * The output integrates directly with Holiday Guard's REST API and materialization engine.
 */
public class ACHProcessingScheduleFactory {

    // ===========================================
    // Main Schedule Definition
    // ===========================================

    /**
     * Creates a complete ACH schedule definition for a given year.
     *
     * <p>This method generates the standard banking "Federal Reserve business days" calendar
     * by combining a WEEKDAYS_ONLY rule with SKIP overrides for all US federal holidays.
     * The resulting schedule definition is ready for direct integration with Holiday Guard's
     * REST API via the {@link ACHScheduleDefinition#toUpdateRequest()} method.
     *
     * @param year the year for which to generate the ACH schedule (must be positive)
     * @return a complete ACHScheduleDefinition containing schedule, rule, and holiday overrides
     * @throws IllegalArgumentException if year is not positive
     *
     * @see ACHScheduleDefinition#toUpdateRequest()
     * @see USFederalHolidays#getHolidays(int)
     */
    public static ACHScheduleDefinition createACHSchedule(int year) {
        Schedule schedule = Schedule.builder()
                .name("ACH Processing")
                .description("ACH file processing on Federal Reserve business days (weekdays excluding federal holidays)")
                .country("US")
                .active(true)
                .build();

        CreateRuleRequest weekdaysRule = new CreateRuleRequest(
                Rule.RuleType.WEEKDAYS_ONLY,
                null, // No config needed for weekdays-only
                LocalDate.of(year, 1, 1), // Effective from start of year
                true
        );

        List<CreateDeviationRequest> holidayOverrides =
                USFederalHolidays.createSkipOverrides(year);

        return new ACHScheduleDefinition(schedule, weekdaysRule, holidayOverrides);
    }

    /**
     * Creates a same-day ACH schedule definition for a given year.
     *
     * <p>This method creates a schedule specifically for same-day ACH processing,
     * which follows the same Federal Reserve business days pattern as regular ACH
     * but with a different name and description for operational clarity.
     *
     * @param year the year for which to generate the same-day ACH schedule
     * @return a complete ACHScheduleDefinition for same-day ACH processing
     * @see #createACHSchedule(int)
     */
    public static ACHScheduleDefinition createSameDayACHSchedule(int year) {
        Schedule schedule = Schedule.builder()
                .name("Same-Day ACH Processing")
                .description("Same-day ACH processing on Federal Reserve business days")
                .country("US")
                .active(true)
                .build();

        CreateRuleRequest weekdaysRule = new CreateRuleRequest(
                Rule.RuleType.WEEKDAYS_ONLY,
                null,
                LocalDate.of(year, 1, 1),
                true
        );

        List<CreateDeviationRequest> holidayOverrides =
                USFederalHolidays.createSkipOverrides(year);

        return new ACHScheduleDefinition(schedule, weekdaysRule, holidayOverrides);
    }

    // ===========================================
    // US Federal Holidays Inner Class
    // ===========================================

    /**
     * Inner class responsible for US Federal holiday calculations and override generation.
     * Encapsulates all holiday logic separate from the main schedule creation.
     */
    public static class USFederalHolidays {

        /**
         * Generates all US Federal holidays for a given year.
         *
         * <p>This method calculates all 11 US federal holidays, including both fixed-date
         * holidays (New Year's Day, Independence Day, etc.) and floating holidays that
         * are calculated using {@link java.time.temporal.TemporalAdjusters}.
         *
         * <p>Juneteenth (June 19) is included for years 2021 and later, when it became
         * a federal holiday.
         *
         * @param year the year for which to calculate federal holidays
         * @return a sorted list of all US federal holidays for the given year
         */
        public static List<LocalDate> getHolidays(int year) {
            List<LocalDate> holidays = new ArrayList<>();

            // Fixed date holidays
            holidays.add(LocalDate.of(year, 1, 1));   // New Year's Day
            holidays.add(LocalDate.of(year, 7, 4));   // Independence Day
            holidays.add(LocalDate.of(year, 11, 11)); // Veterans Day
            holidays.add(LocalDate.of(year, 12, 25)); // Christmas Day

            // Juneteenth (federal holiday starting 2021)
            if (year >= 2021) {
                holidays.add(LocalDate.of(year, 6, 19)); // Juneteenth
            }

            // Floating holidays calculated using TemporalAdjusters
            holidays.add(getMartinLutherKingJrDay(year));
            holidays.add(getPresidentsDay(year));
            holidays.add(getMemorialDay(year));
            holidays.add(getLaborDay(year));
            holidays.add(getColumbusDay(year));
            holidays.add(getThanksgiving(year));

            return holidays.stream().sorted().toList();
        }

        /**
         * Creates SKIP overrides for all federal holidays in a year.
         *
         * <p>This method generates {@link CreateDeviationRequest} objects for each
         * federal holiday in the given year, configured as SKIP actions with descriptive
         * reasons. The overrides are permanent (no expiration) and created by "system".
         *
         * <p>Output integrates directly with Holiday Guard's override system and can be
         * used in {@link UpdateRuleRequest}.
         *
         * @param year the year for which to create holiday skip overrides
         * @return a list of CreateScheduleOverrideRequest objects for all federal holidays
         * @see #getHolidays(int)
         */
        public static List<CreateDeviationRequest> createSkipOverrides(int year) {
            return getHolidays(year)
                    .stream()
                    .map(holiday -> new CreateDeviationRequest(
                            holiday,
                            RunStatus.FORCE_SKIP,
                            "Federal Holiday: " + getHolidayName(holiday, year),
                            "system",
                            null // never expires
                    )).toList();
        }

        // ===========================================
        // Holiday Calculation Methods
        // ===========================================

        private static LocalDate getMartinLutherKingJrDay(int year) {
            // Third Monday in January
            return LocalDate.of(year, 1, 1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY));
        }

        private static LocalDate getPresidentsDay(int year) {
            // Third Monday in February
            return LocalDate.of(year, 2, 1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY));
        }

        private static LocalDate getMemorialDay(int year) {
            // Last Monday in May
            return LocalDate.of(year, 5, 31)
                    .with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY));
        }

        private static LocalDate getLaborDay(int year) {
            // First Monday in September
            return LocalDate.of(year, 9, 1)
                    .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        }

        private static LocalDate getColumbusDay(int year) {
            // Second Monday in October
            return LocalDate.of(year, 10, 1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.MONDAY));
        }

        private static LocalDate getThanksgiving(int year) {
            // Fourth Thursday in November
            return LocalDate.of(year, 11, 1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        }

        /**
         * Gets human-readable name for a federal holiday.
         */
        private static String getHolidayName(LocalDate date, int year) {
            if (date.equals(LocalDate.of(year, 1, 1))) return "New Year's Day";
            if (date.equals(getMartinLutherKingJrDay(year))) return "Martin Luther King Jr. Day";
            if (date.equals(getPresidentsDay(year))) return "Presidents' Day";
            if (date.equals(getMemorialDay(year))) return "Memorial Day";
            if (year >= 2021 && date.equals(LocalDate.of(year, 6, 19))) return "Juneteenth";
            if (date.equals(LocalDate.of(year, 7, 4))) return "Independence Day";
            if (date.equals(getLaborDay(year))) return "Labor Day";
            if (date.equals(getColumbusDay(year))) return "Columbus Day";
            if (date.equals(LocalDate.of(year, 11, 11))) return "Veterans Day";
            if (date.equals(getThanksgiving(year))) return "Thanksgiving Day";
            if (date.equals(LocalDate.of(year, 12, 25))) return "Christmas Day";

            return "Federal Holiday";
        }
    }

    // ===========================================
    // ACH Schedule Definition Data Class
    // ===========================================

    /**
     * Data class that holds a complete ACH schedule definition ready for Holiday Guard integration.
     * Contains everything needed for the REST API: schedule + rule + overrides.
     */
    public static class ACHScheduleDefinition {
        private final Schedule schedule;
        private final CreateRuleRequest rule;
        private final List<CreateDeviationRequest> holidayOverrides;

        public ACHScheduleDefinition(Schedule schedule, CreateRuleRequest rule, List<CreateDeviationRequest> holidayOverrides) {
            this.schedule = schedule;
            this.rule = rule;
            this.holidayOverrides = holidayOverrides;
        }

        public Schedule getSchedule() {
            return schedule;
        }

        public CreateRuleRequest getRule() {
            return rule;
        }

        public List<CreateDeviationRequest> getHolidayOverrides() {
            return holidayOverrides;
        }

        /**
         * Creates an UpdateScheduleRuleRequest for direct use with Holiday Guard REST API.
         *
         * <p>This method converts the ACH schedule definition into a format that can be
         * directly submitted to the {@code /api/v1/schedules/{scheduleId}/versions} endpoint.
         * The effective date is set to the beginning of the year specified in the rule.
         *
         * @return an UpdateScheduleRuleRequest ready for REST API submission
         * @see com.jw.holidayguard.controller.ScheduleVersionController#updateScheduleRule
         */
        public UpdateRuleRequest toUpdateRequest() {
            return new UpdateRuleRequest(
                    rule.getEffectiveFrom().atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
                    rule,
                    holidayOverrides
            );
        }
    }
}
