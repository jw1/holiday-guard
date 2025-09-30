package com.jw.holidayguard.dto.request;

import com.jw.holidayguard.domain.Rule;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRuleRequest {

    @NotNull(message = "Rule type is required")
    private Rule.RuleType ruleType;

    private String ruleConfig;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private boolean active = true;
}
