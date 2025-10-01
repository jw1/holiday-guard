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
public class UpdateRuleRequest {

    private Instant effectiveFrom;

    @Valid
    @NotNull(message = "A rule is required")
    private CreateRuleRequest rule;

    @Valid
    private List<CreateDeviationRequest> deviations;
}
