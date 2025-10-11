package com.jw.holidayguard.domain;

/**
 * Enumeration of possible run statuses for a schedule on a given date.
 */
public enum RunStatus {

    /**
     * Schedule should run on this date (based on rule evaluation).
     */
    RUN,

    /**
     * Schedule should not run on this date (based on rule evaluation).
     */
    SKIP,

    /**
     * Deviation forces the schedule to run regardless of rule.
     */
    FORCE_RUN,

    /**
     * Deviation forces the schedule to skip regardless of rule.
     */
    FORCE_SKIP;

    /**
     * Determines the appropriate RunStatus based on shouldRun result and optional deviation.
     *
     * @param shouldRun result from schedule rule evaluation
     * @return The appropriate RunStatus
     */
    public static RunStatus fromCalendar(boolean shouldRun) {
        return fromCalendar(shouldRun, null);
    }

    /**
     * Determines the appropriate RunStatus based on shouldRun result and deviation.
     *
     * <p>If a deviation exists, it directly contains the forced run status (FORCE_RUN or FORCE_SKIP).
     * Otherwise, the status is determined by the rule evaluation result.
     *
     * @param shouldRun result from schedule rule evaluation
     * @param deviation deviation (if applicable), which already contains the forced RunStatus
     * @return The appropriate RunStatus
     */
    public static RunStatus fromCalendar(boolean shouldRun, Deviation deviation) {

        // deviation takes precedence and already contains the RunStatus
        if (null != deviation) {
            return deviation.getAction(); // Already FORCE_RUN or FORCE_SKIP
        }

        return shouldRun ? RUN : SKIP;
    }
}
