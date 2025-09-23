package com.jw.holidayguard.dto;

import com.jw.holidayguard.domain.ScheduleRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRuleResponse {

    private UUID id;
    private UUID scheduleId;
    private UUID versionId;
    private ScheduleRule.RuleType ruleType;
    private String ruleConfig;
    private LocalDate effectiveFrom;
    private Instant createdAt;
    private boolean active;
}
