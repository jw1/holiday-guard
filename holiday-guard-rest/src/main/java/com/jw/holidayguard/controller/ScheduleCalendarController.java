package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.ScheduleMonthDto;
import com.jw.holidayguard.dto.DeviationDto;
import com.jw.holidayguard.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;


@RestController
@RequestMapping("/api/v1/schedules/{scheduleId}")
@RequiredArgsConstructor
public class ScheduleCalendarController {

    private final ScheduleService scheduleService;

    @GetMapping("/calendar")
    public ScheduleMonthDto getScheduleCalendar(
            @PathVariable Long scheduleId,
            @RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return scheduleService.getScheduleCalendar(scheduleId, yearMonth);
    }

    @GetMapping("/deviations")
    public List<DeviationDto> getScheduleDeviations(@PathVariable Long scheduleId) {
        return scheduleService.getScheduleDeviations(scheduleId);
    }
}
