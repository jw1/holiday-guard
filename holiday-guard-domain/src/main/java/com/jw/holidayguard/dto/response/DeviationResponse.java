package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.Deviation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviationResponse {
    
    private UUID id;
    private UUID scheduleId;
    private UUID versionId;
    private LocalDate overrideDate;
    private Deviation.Action action;
    private String reason;
    private String createdBy;
    private Instant createdAt;
    private LocalDate expiresAt;
}