package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.Rule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {

    private UUID id;
    private UUID scheduleId;
    private UUID versionId;
    private Rule.RuleType ruleType;
    private String ruleConfig;
    private LocalDate effectiveFrom;
    private Instant createdAt;
    private boolean active;
}
