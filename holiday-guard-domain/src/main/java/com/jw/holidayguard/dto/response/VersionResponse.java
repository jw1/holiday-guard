package com.jw.holidayguard.dto.response;

import java.time.Instant;
import java.util.List;

public record VersionResponse(
    Long id,
    Long scheduleId,
    Instant effectiveFrom,
    Instant createdAt,
    boolean active,
    List<RuleResponse> rules,
    List<DeviationResponse> deviations
) {}
