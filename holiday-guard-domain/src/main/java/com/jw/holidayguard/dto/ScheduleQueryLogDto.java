package com.jw.holidayguard.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for representing a schedule query log entry in the API.
 */
public record ScheduleQueryLogDto(
    UUID logId,
    UUID scheduleId,
    String scheduleName,
    UUID versionId,
    LocalDate queryDate,
    boolean shouldRunResult,
    String reason,
    boolean overrideApplied,
    String clientIdentifier,
    Instant createdAt) {
}
