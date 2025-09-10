package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.ShouldRunQueryResponse;
import com.jw.holidayguard.service.ScheduleQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
@Validated
public class ShouldRunController {
    
    private final ScheduleQueryService scheduleQueryService;

    public ShouldRunController(ScheduleQueryService scheduleQueryService) {
        this.scheduleQueryService = scheduleQueryService;
    }

    /**
     * Simple "should I run today?" endpoint - the primary daily use case.
     * No request body needed, defaults to today's date.
     * 
     * Example: GET /api/v1/schedules/{scheduleId}/should-run?client=payroll-service
     */
    @GetMapping("/{scheduleId}/should-run")
    public ResponseEntity<ShouldRunQueryResponse> shouldRunToday(
            @PathVariable UUID scheduleId,
            @RequestParam(required = false) String client) {
        
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(LocalDate.now(), client);
        ShouldRunQueryResponse response = scheduleQueryService.shouldRunToday(scheduleId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * "Should I run on a specific date?" endpoint for advanced use cases.
     * Requires request body with date and optional client identifier.
     * 
     * Example: POST /api/v1/schedules/{scheduleId}/should-run
     * Body: {"queryDate": "2024-03-15", "clientIdentifier": "payroll-service"}
     */
    @PostMapping("/{scheduleId}/should-run")
    public ResponseEntity<ShouldRunQueryResponse> shouldRunOnDate(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody ShouldRunQueryRequest request) {
        
        ShouldRunQueryResponse response = scheduleQueryService.shouldRunToday(scheduleId, request);
        
        return ResponseEntity.ok(response);
    }
}