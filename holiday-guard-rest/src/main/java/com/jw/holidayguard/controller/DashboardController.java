package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.DailyScheduleStatusDto;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final ScheduleQueryService scheduleQueryService;

    public DashboardController(ScheduleQueryService scheduleQueryService) {
        this.scheduleQueryService = scheduleQueryService;
    }

    @GetMapping("/schedule-status")
    public List<DailyScheduleStatusDto> getStatusToday() {
        return scheduleQueryService.getDailyRunStatusForAllActiveSchedules();
    }

    @GetMapping("/stats/total-schedules")
    public Map<String, Long> getTotalSchedulesCount() {
        return Map.of("count", scheduleQueryService.getTotalSchedulesCount());
    }

    @GetMapping("/stats/active-schedules")
    public Map<String, Long> getActiveSchedulesCount() {
        return Map.of("count", scheduleQueryService.getActiveSchedulesCount());
    }
}
