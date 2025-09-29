package com.jw.holidayguard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleVersionResponse {
    
    private UUID id;
    private UUID scheduleId;
    private Instant effectiveFrom;
    private Instant createdAt;
    private boolean active;
    private List<ScheduleRuleResponse> rules;
    private List<ScheduleOverrideResponse> overrides;
}