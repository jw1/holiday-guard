package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.ScheduleQueryLogDto;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final ScheduleQueryService scheduleQueryService;

    public AuditLogController(ScheduleQueryService scheduleQueryService) {
        this.scheduleQueryService = scheduleQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleQueryLogDto>> getAllLogs() {
        List<ScheduleQueryLogDto> logs = scheduleQueryService.findAllLogs();
        return ResponseEntity.ok(logs);
    }
}
