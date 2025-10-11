package com.jw.holidayguard.dto.response;

import com.jw.holidayguard.domain.RunStatus;
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
    private RunStatus runStatus;
    private String reason;
    private boolean deviationApplied;
    private Long versionId;
}