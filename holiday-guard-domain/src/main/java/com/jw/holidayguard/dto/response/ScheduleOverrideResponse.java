package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.ScheduleOverride;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleOverrideResponse {
    
    private UUID id;
    private UUID scheduleId;
    private UUID versionId;
    private LocalDate overrideDate;
    private ScheduleOverride.OverrideAction action;
    private String reason;
    private String createdBy;
    private Instant createdAt;
    private LocalDate expiresAt;
}