package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.ScheduleRules;
import java.time.LocalDate;
import java.util.List;

/**
 * Core interface for processing schedule rules and generating materialized calendar dates.
 * Each rule type (WEEKDAYS_ONLY, CRON_EXPRESSION, etc.) has a specific handler that 
 * implements the date generation logic for that rule type.
 */
public interface RuleEngine {
    
    /**
     * Generates a list of dates when a schedule should run based on the given rule
     * within the specified date range.
     *
     * @param rule The schedule rule containing type and configuration
     * @param fromDate Start date (inclusive) for date generation
     * @param toDate End date (inclusive) for date generation
     * @return List of dates when the schedule should run, sorted chronologically
     */
    List<LocalDate> generateDates(ScheduleRules rule, LocalDate fromDate, LocalDate toDate);
}