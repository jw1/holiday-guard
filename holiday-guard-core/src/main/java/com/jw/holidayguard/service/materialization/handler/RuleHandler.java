package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRules;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for handling specific rule types in the materialization engine.
 * Each rule type (WEEKDAYS_ONLY, CRON_EXPRESSION, etc.) has its own handler
 * implementing the specific logic for that rule type.
 */
public interface RuleHandler {
    
    /**
     * Generates dates for this specific rule type within the given range.
     *
     * @param rule The schedule rule (may be null for simple handlers like weekdays-only)
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return List of dates when the schedule should run, sorted chronologically
     */
    List<LocalDate> generateDates(ScheduleRules rule, LocalDate fromDate, LocalDate toDate);
    
    /**
     * Returns the rule type this handler supports.
     */
    ScheduleRules.RuleType getSupportedRuleType();
}