package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.Rule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {

    private Long id;
    private Long scheduleId;
    private Long versionId;
    private Rule.RuleType ruleType;
    private String ruleConfig;
    private LocalDate effectiveFrom;
    private Instant createdAt;
    private boolean active;
}
