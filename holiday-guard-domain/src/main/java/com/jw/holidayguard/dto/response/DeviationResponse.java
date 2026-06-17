package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.RunStatus;

import java.time.Instant;
import java.time.LocalDate;

public record DeviationResponse(
    Long id,
    Long scheduleId,
    Long versionId,
    LocalDate deviationDate,
    RunStatus action,
    String reason,
    String createdBy,
    Instant createdAt,
    LocalDate expiresAt
) {}
