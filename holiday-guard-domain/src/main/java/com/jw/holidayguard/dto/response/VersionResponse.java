package com.jw.holidayguard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersionResponse {

    private Long id;
    private Long scheduleId;
    private Instant effectiveFrom;
    private Instant createdAt;
    private boolean active;
    private List<RuleResponse> rules;
    private List<DeviationResponse> overrides;
}