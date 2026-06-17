package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.Rule;

import java.time.Instant;
import java.time.LocalDate;

public record RuleResponse(
    Long id,
    Long scheduleId,
    Long versionId,
    Rule.RuleType ruleType,
    String ruleConfig,
    LocalDate effectiveFrom,
    Instant createdAt,
    boolean active
) {}
