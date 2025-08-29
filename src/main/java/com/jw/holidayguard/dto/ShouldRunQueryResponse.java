package com.jw.holidayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShouldRunQueryResponse {
    
    private UUID scheduleId;
    private LocalDate queryDate;
    private boolean shouldRun;
    private String reason;
    private boolean overrideApplied;
    private UUID versionId;
}