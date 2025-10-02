package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.Deviation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviationResponse {

    private Long id;
    private Long scheduleId;
    private Long versionId;
    private LocalDate overrideDate;
    private Deviation.Action action;
    private String reason;
    private String createdBy;
    private Instant createdAt;
    private LocalDate expiresAt;
}