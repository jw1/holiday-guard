package com.jw.holidayguard.service.rule;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for applying schedule overrides to materialized calendar dates.
 * Handles SKIP and FORCE_RUN override actions with proper precedence logic.
 */
public interface OverrideApplicator {

    /**
     * Applies active overrides to the list of rule-generated dates within the specified range.
     *
     * @param scheduleId The schedule ID
     * @param versionId The version ID
     * @param ruleDates The dates generated from schedule rules
     * @param fromDate Start date for override application
     * @param toDate End date for override application
     * @return Final list of dates after applying overrides (sorted chronologically)
     */
    List<LocalDate> applyOverrides(Long scheduleId, Long versionId, List<LocalDate> ruleDates, LocalDate fromDate, LocalDate toDate);
}