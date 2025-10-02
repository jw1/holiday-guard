package com.jw.holidayguard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryLogResponse {

    private Long id;
    private Long scheduleId;
    private Long versionId;
    private LocalDate queryDate;
    private boolean shouldRunResult;
    private String reason;
    private boolean overrideApplied;
    private Instant queriedAt;
    private String clientIdentifier;
}