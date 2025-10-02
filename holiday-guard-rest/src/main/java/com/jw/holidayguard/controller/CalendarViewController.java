package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.MultiScheduleCalendarDto;
import com.jw.holidayguard.service.CalendarViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * REST controller for the multi-schedule calendar viewer.
 * Provides endpoints to fetch aggregated calendar data across multiple schedules.
 */
@RestController
@RequestMapping("/api/v1/calendar-view")
@RequiredArgsConstructor
public class CalendarViewController {

    private final CalendarViewService calendarViewService;

    /**
     * Get calendar data for multiple schedules for a given month.
     *
     * @param scheduleIds Comma-separated list of schedule IDs
     * @param yearMonth Year-month in format yyyy-MM
     * @param includeDeviations Whether to include deviations (default: true)
     * @return MultiScheduleCalendarDto with all calendar days
     */
    @GetMapping
    public MultiScheduleCalendarDto getMultiScheduleCalendar(
            @RequestParam("scheduleIds") List<Long> scheduleIds,
            @RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(value = "includeDeviations", defaultValue = "true") boolean includeDeviations) {

        return calendarViewService.getMultiScheduleCalendar(scheduleIds, yearMonth, includeDeviations);
    }
}
