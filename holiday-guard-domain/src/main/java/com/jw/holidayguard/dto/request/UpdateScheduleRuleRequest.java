package com.jw.holidayguard.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRuleRequest {

    private Instant effectiveFrom;

    @Valid
    @NotNull(message = "A rule is required")
    private CreateScheduleRuleRequest rule;

    @Valid
    private List<CreateScheduleOverrideRequest> overrides;
}
