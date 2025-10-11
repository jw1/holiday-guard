package com.jw.holidayguard.controller;

import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import com.jw.holidayguard.dto.response.VersionResponse;
import com.jw.holidayguard.repository.ConditionalOnManagement;
import com.jw.holidayguard.service.ScheduleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for schedule version management operations.
 *
 * <p>This controller is only active when the repository implementation supports
 * management operations. Handles creating new versions and updating rules.
 */
@RestController
@RequestMapping("/api/v1/schedules")
@Validated
@ConditionalOnManagement
public class ScheduleVersionController {

    private final ScheduleVersionService scheduleVersionService;

    public ScheduleVersionController(ScheduleVersionService scheduleVersionService) {
        this.scheduleVersionService = scheduleVersionService;
    }

    @PostMapping("/{scheduleId}/versions")
    public ResponseEntity<VersionResponse> updateScheduleRule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateRuleRequest request) {

        Version newVersion = scheduleVersionService.updateScheduleRule(scheduleId, request);

        VersionResponse response = new VersionResponse(
                newVersion.getId(),
                newVersion.getScheduleId(),
                newVersion.getEffectiveFrom(),
                newVersion.getCreatedAt(),
                newVersion.isActive(),
                null, // rules - would populate in full implementation
                null  // deviations - would populate in full implementation
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
