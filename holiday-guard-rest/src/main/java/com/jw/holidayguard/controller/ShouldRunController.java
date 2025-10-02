package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.request.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.response.ShouldRunQueryResponse;
import com.jw.holidayguard.service.ScheduleQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/schedules")
@Validated
public class ShouldRunController {

    private final ScheduleQueryService service;

    public ShouldRunController(ScheduleQueryService service) {
        this.service = service;
    }

    /**
     * Simple "should I run today?" endpoint - the primary daily use case.
     * No request body needed, defaults to today's date.
     * <p>
     * Example: GET /api/v1/schedules/{scheduleId}/should-run?client=payroll-service
     */
    @GetMapping("/{scheduleId}/should-run")
    public ResponseEntity<ShouldRunQueryResponse> shouldRunToday(
            @PathVariable Long scheduleId,
            @RequestParam(required = false) String clientIdentifier) {

        var request = new ShouldRunQueryRequest(clientIdentifier);
        var response = service.shouldRunToday(scheduleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * "Should I run on a specific date?" endpoint for advanced use cases.
     * Requires request body with date and optional client identifier.
     * <p>
     * Example: POST /api/v1/schedules/{scheduleId}/should-run
     * Body: {"queryDate": "2024-03-15", "clientIdentifier": "payroll-service"}
     */
    @PostMapping("/{scheduleId}/should-run")
    public ResponseEntity<ShouldRunQueryResponse> shouldRunOnDate(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ShouldRunQueryRequest request) {

        var response = service.shouldRunToday(scheduleId, request);
        return ResponseEntity.ok(response);
    }
}