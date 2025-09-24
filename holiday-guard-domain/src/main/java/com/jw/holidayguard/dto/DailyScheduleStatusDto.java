package com.jw.holidayguard.dto;

import java.util.UUID;

/**
 * DTO representing the daily run status of a single schedule.
 *
 * @param scheduleId   The unique identifier of the schedule.
 * @param scheduleName The name of the schedule.
 * @param shouldRun    A boolean indicating if the schedule is expected to run today.
 */
public record DailyScheduleStatusDto(
    UUID scheduleId,
    String scheduleName,
    boolean shouldRun,
    String reason
) {
}
