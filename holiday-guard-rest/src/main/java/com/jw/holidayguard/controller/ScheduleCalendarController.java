package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.ScheduleCalendarDto;
import com.jw.holidayguard.dto.ScheduleOverrideDto;
import com.jw.holidayguard.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules/{scheduleId}")
@RequiredArgsConstructor
public class ScheduleCalendarController {

    private final ScheduleService scheduleService;

    @GetMapping("/calendar")
    public ScheduleCalendarDto getScheduleCalendar(
            @PathVariable UUID scheduleId,
            @RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return scheduleService.getScheduleCalendar(scheduleId, yearMonth);
    }

    @GetMapping("/overrides")
    public List<ScheduleOverrideDto> getScheduleOverrides(@PathVariable UUID scheduleId) {
        return scheduleService.getScheduleOverrides(scheduleId);
    }
}
