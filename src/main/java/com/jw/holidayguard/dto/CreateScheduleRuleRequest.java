package com.jw.holidayguard.dto;

import com.jw.holidayguard.domain.ScheduleRules;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRuleRequest {
    
    @NotNull(message = "Rule type is required")
    private ScheduleRules.RuleType ruleType;
    
    private String ruleConfig;
    
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;
    
    private boolean active = true;
}