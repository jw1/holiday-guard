package com.jw.holidayguard.dto.view;

import java.time.YearMonth;
import java.util.List;

/**
 * Calendar view for multiple schedules over a month.
 *
 * <p>This is the top-level normalized view for the calendar viewer UI.
 * Each schedule appears once with all its days nested underneath, eliminating
 * the redundancy of repeating schedule metadata for every day.
 *
 * <p>Structure:
 * <pre>
 * MultiScheduleCalendarView
 * └── schedules: List&lt;ScheduleMonthView&gt;
 *     └── days: List&lt;DayStatusView&gt;
 * </pre>
 *
 * <p>This provides a ~70% reduction in JSON payload size compared to the
 * flat structure where schedule metadata is repeated for every day.
 */
public record MultiScheduleCalendarView(
    YearMonth yearMonth,
    List<ScheduleMonthView> schedules
) {}
