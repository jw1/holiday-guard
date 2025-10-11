package com.jw.holidayguard.dto;

import com.jw.holidayguard.domain.RunStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a single calendar day for a specific schedule.
 * Used in the multi-schedule calendar view to show run/no-run status with deviations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDayDto {
    private Long scheduleId;
    private String scheduleName;
    private LocalDate date;
    private RunStatus status;
    private String reason; // Deviation reason, if applicable
}
