package com.jw.holidayguard.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for representing a schedule query log entry in the API.
 */
public record QueryLogDto(
    Long logId,
    Long scheduleId,
    String scheduleName,
    Long versionId,
    LocalDate queryDate,
    boolean shouldRunResult,
    String reason,
    boolean overrideApplied,
    String clientIdentifier,
    Instant createdAt) {
}
