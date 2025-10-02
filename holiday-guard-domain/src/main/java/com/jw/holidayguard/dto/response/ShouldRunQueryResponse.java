package com.jw.holidayguard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShouldRunQueryResponse {

    private Long scheduleId;
    private LocalDate queryDate;
    private boolean shouldRun;
    private String reason;
    private boolean overrideApplied;
    private Long versionId;
}