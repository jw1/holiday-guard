package com.jw.holidayguard.util;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.dto.request.CreateDeviationRequest;
import com.jw.holidayguard.dto.request.CreateRuleRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating test schedule data.
 * Provides static methods for generating different types of schedules and related entities
 * commonly used in testing scenarios.
 */
public class ScheduleTestDataFactory {

    // ===========================================
    // Schedule Factory Methods
    // ===========================================

    /**
     * Creates a basic weekdays-only payroll schedule.
     */
    public static Schedule createPayrollSchedule() {
        return Schedule.builder()
                .name("Weekly Payroll")
                .description("Standard weekly payroll processing")
                .country("US")
                .active(true)
                .build();
    }

    /**
     * Creates a bi-weekly payroll schedule (every other Friday).
     */
    public static Schedule createBiWeeklyPayrollSchedule() {
        return Schedule.builder()
                .name("Bi-Weekly Payroll")
                .description("Bi-weekly payroll processing every other Friday")
                .country("US")
                .active(true)
                .build();
    }

    /**
     * Creates an ACH processing schedule (weekdays, excluding bank holidays).
     */
    public static Schedule createACHSchedule() {
        return Schedule.builder()
                .name("ACH Processing")
                .description("ACH file processing on Federal Reserve business days")
                .country("US")
                .active(true)
                .build();
    }

    /**
     * Creates a monthly reports schedule (first business day of month).
     */
    public static Schedule createMonthlyReportsSchedule() {
        return Schedule.builder()
                .name("Monthly Financial Reports")
                .description("Monthly financial reports on first business day")
                .country("US")
                .active(true)
                .build();
    }

    /**
     * Creates a quarterly compliance schedule.
     */
    public static Schedule createQuarterlyComplianceSchedule() {
        return Schedule.builder()
                .name("Quarterly Compliance Reports")
                .description("Quarterly compliance reports due 15th of month after quarter end")
                .country("US")
                .active(true)
                .build();
    }

    // ===========================================
    // Schedule Rules Factory Methods
    // ===========================================

    /**
     * Creates a simple weekdays-only rule (Monday-Friday).
     */
    public static CreateRuleRequest createWeekdaysOnlyRule() {
        return new CreateRuleRequest(
                Rule.RuleType.WEEKDAYS_ONLY,
                null, // No config needed for weekdays-only
                LocalDate.now(),
                true
        );
    }

    /**
     * Creates a cron rule for specific time and days.
     * Example: Every weekday at 9 AM.
     */
    public static CreateRuleRequest createCronRule(String cronExpression) {
        return new CreateRuleRequest(
                Rule.RuleType.CRON_EXPRESSION,
                cronExpression, // e.g., "0 0 9 * * MON-FRI"
                LocalDate.now(),
                true
        );
    }

    /**
     * Creates a bi-weekly rule (every other Friday starting from a base date).
     */
    public static CreateRuleRequest createBiWeeklyRule(LocalDate startDate) {
        return new CreateRuleRequest(
                Rule.RuleType.CRON_EXPRESSION,
                "0 0 9 ? * FRI", // Every Friday at 9 AM - logic would handle bi-weekly in service
                startDate,
                true
        );
    }

    // ===========================================
    // Schedule Override Factory Methods
    // ===========================================

    /**
     * Creates a holiday skip override.
     */
    public static CreateDeviationRequest createHolidaySkipOverride(LocalDate holidayDate, String holidayName) {
        return new CreateDeviationRequest(
                holidayDate,
                Deviation.Action.SKIP,
                "Holiday skip: " + holidayName,
                "system",
                null // No expiration - permanent holiday
        );
    }

    /**
     * Creates a temporary skip override (with expiration).
     */
    public static CreateDeviationRequest createTemporarySkipOverride(LocalDate skipDate, String reason, LocalDate expiresAt) {
        return new CreateDeviationRequest(
                skipDate,
                Deviation.Action.SKIP,
                "Temporary skip: " + reason,
                "admin",
                expiresAt
        );
    }

    /**
     * Creates a force-run override for emergency processing.
     */
    public static CreateDeviationRequest createEmergencyRunOverride(LocalDate runDate, String reason) {
        return new CreateDeviationRequest(
                runDate,
                Deviation.Action.FORCE_RUN,
                "Emergency run: " + reason,
                "admin",
                null
        );
    }

    // ===========================================
    // Common Holiday Dates (US Federal)
    // ===========================================

    /**
     * Gets common US federal holiday dates for current year.
     */
    public static List<LocalDate> getUSFederalHolidays(int year) {
        return List.of(
                LocalDate.of(year, 1, 1),   // New Year's Day
                // MLK Day (3rd Monday of January)
                LocalDate.of(year, 1, 15).with(java.time.temporal.TemporalAdjusters.dayOfWeekInMonth(3, java.time.DayOfWeek.MONDAY)),
                // Presidents Day (3rd Monday of February)
                LocalDate.of(year, 2, 15).with(java.time.temporal.TemporalAdjusters.dayOfWeekInMonth(3, java.time.DayOfWeek.MONDAY)),
                // Memorial Day (last Monday of May)
                LocalDate.of(year, 5, 31).with(java.time.temporal.TemporalAdjusters.lastInMonth(java.time.DayOfWeek.MONDAY)),
                LocalDate.of(year, 7, 4),   // Independence Day
                // Labor Day (1st Monday of September)
                LocalDate.of(year, 9, 7).with(java.time.temporal.TemporalAdjusters.firstInMonth(java.time.DayOfWeek.MONDAY)),
                // Columbus Day (2nd Monday of October)
                LocalDate.of(year, 10, 8).with(java.time.temporal.TemporalAdjusters.dayOfWeekInMonth(2, java.time.DayOfWeek.MONDAY)),
                LocalDate.of(year, 11, 11), // Veterans Day
                // Thanksgiving (4th Thursday of November)
                LocalDate.of(year, 11, 22).with(java.time.temporal.TemporalAdjusters.dayOfWeekInMonth(4, java.time.DayOfWeek.THURSDAY)),
                LocalDate.of(year, 12, 25)  // Christmas Day
        );
    }

    // ===========================================
    // Entity Builder Methods (for complex tests)
    // ===========================================

    /**
     * Creates a ScheduleVersion entity for testing.
     */
    public static Version createScheduleVersion(UUID scheduleId, boolean active) {
        return Version.builder()
                .scheduleId(scheduleId)
                .effectiveFrom(Instant.now())
                .active(active)
                .build();
    }


    /**
     * Creates a ScheduleQueryLog entry for testing.
     */
    public static QueryLog createQueryLog(UUID scheduleId, UUID versionId, LocalDate queryDate, boolean result, String reason) {
        return QueryLog.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .queryDate(queryDate)
                .shouldRunResult(result)
                .reason(reason)
                .deviationApplied(false)
                .clientIdentifier("test-client")
                .build();
    }
}
