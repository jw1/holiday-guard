package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.RunStatus;

import java.time.LocalDate;

public record ShouldRunQueryResponse(
    Long scheduleId,
    LocalDate queryDate,
    boolean shouldRun,
    RunStatus runStatus,
    String reason,
    boolean deviationApplied,
    Long versionId
) {}
