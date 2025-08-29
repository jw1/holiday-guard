package com.jw.holidayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleQueryLogResponse {
    
    private UUID id;
    private UUID scheduleId;
    private UUID versionId;
    private LocalDate queryDate;
    private boolean shouldRunResult;
    private String reason;
    private boolean overrideApplied;
    private Instant queriedAt;
    private String clientIdentifier;
}