package com.jw.holidayguard.dto.view;

import java.time.YearMonth;
import java.util.List;

/**
 * Calendar view for a single schedule over a month.
 *
 * <p>This normalized view includes schedule metadata once (id and name),
 * with all days for that schedule nested underneath. This eliminates the
 * redundancy of repeating schedule information for every day.
 *
 * <p>Used as a building block for multi-schedule calendar views.
 */
public record ScheduleMonthView(
    Long scheduleId,
    String scheduleName,
    YearMonth yearMonth,
    List<DayStatusView> days
) {}
