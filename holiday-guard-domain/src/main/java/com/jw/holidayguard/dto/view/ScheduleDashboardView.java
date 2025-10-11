package com.jw.holidayguard.dto.view;

import com.jw.holidayguard.domain.RunStatus;

/**
 * Dashboard status view for a single schedule (typically for "today").
 *
 * <p>This view includes both the detailed {@link RunStatus} enum (which provides
 * information about RUN, SKIP, FORCE_RUN, FORCE_SKIP, NO_DAYS) and a convenience
 * {@code shouldRun} boolean for simple yes/no rendering.
 *
 * <p>The {@code shouldRun} field is derived from the status using {@link RunStatus#shouldRun()}.
 */
public record ScheduleDashboardView(
    Long scheduleId,
    String scheduleName,
    RunStatus status,      // detailed status enum
    boolean shouldRun,     // convenience: status.shouldRun()
    String reason
) {}
