package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.QueryLogDto;
import com.jw.holidayguard.repository.ConditionalOnManagement;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for audit log viewing.
 *
 * <p>Query logs are only available with management-enabled implementations
 * that persist audit trails to a database.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@ConditionalOnManagement
public class AuditLogController {

    private final ScheduleQueryService scheduleQueryService;

    public AuditLogController(ScheduleQueryService scheduleQueryService) {
        this.scheduleQueryService = scheduleQueryService;
    }

    @GetMapping
    public ResponseEntity<List<QueryLogDto>> getAllLogs() {
        List<QueryLogDto> logs = scheduleQueryService.findAllLogs();
        return ResponseEntity.ok(logs);
    }
}
