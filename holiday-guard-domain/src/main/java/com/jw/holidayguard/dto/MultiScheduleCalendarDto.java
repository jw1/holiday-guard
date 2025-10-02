package com.jw.holidayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

/**
 * Container for calendar data across multiple schedules for a given month.
 * Used by the calendar viewer to display multiple schedules at once.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiScheduleCalendarDto {
    private YearMonth yearMonth;
    private List<CalendarDayDto> days;
}
