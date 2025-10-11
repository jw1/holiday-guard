package com.jw.holidayguard.service.rule;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for applying schedule deviations to materialized calendar dates.
 * Handles SKIP and FORCE_RUN deviation actions with proper precedence logic.
 */
public interface DeviationApplicator {

    /**
     * Applies active deviations to the list of rule-generated dates within the specified range.
     *
     * @param scheduleId The schedule ID
     * @param versionId The version ID
     * @param ruleDates The dates generated from schedule rules
     * @param from Start date for deviation application
     * @param to End date for deviation application
     *
     * @return Final list of dates after applying overrides (sorted chronologically)
     */
    List<LocalDate> applyDeviations(Long scheduleId, Long versionId, List<LocalDate> ruleDates, LocalDate from, LocalDate to);
}