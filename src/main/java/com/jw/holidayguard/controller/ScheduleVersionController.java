package com.jw.holidayguard.controller;

import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.UpdateScheduleRulesRequest;
import com.jw.holidayguard.dto.ScheduleVersionResponse;
import com.jw.holidayguard.service.ScheduleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
@Validated
public class ScheduleVersionController {
    
    private final ScheduleVersionService scheduleVersionService;

    public ScheduleVersionController(ScheduleVersionService scheduleVersionService) {
        this.scheduleVersionService = scheduleVersionService;
    }

    @PostMapping("/{scheduleId}/versions")
    public ResponseEntity<ScheduleVersionResponse> updateScheduleRules(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateScheduleRulesRequest request) {
        
        ScheduleVersion newVersion = scheduleVersionService.updateScheduleRules(scheduleId, request);
        
        ScheduleVersionResponse response = new ScheduleVersionResponse(
            newVersion.getId(),
            newVersion.getScheduleId(),
            newVersion.getEffectiveFrom(),
            newVersion.getCreatedAt(),
            newVersion.isActive(),
            null, // rules - would populate in full implementation
            null  // overrides - would populate in full implementation
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}