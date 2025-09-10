package com.jw.holidayguard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRulesRequest {
    
    private Instant effectiveFrom;
    
    @Valid
    @NotEmpty(message = "At least one rule is required")
    private List<CreateScheduleRuleRequest> rules;
    
    @Valid
    private List<CreateScheduleOverrideRequest> overrides;
}