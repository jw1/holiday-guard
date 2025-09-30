package com.jw.holidayguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
public class CreateScheduleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Country is required")
    @Builder.Default
    private String country = "US";

    @Builder.Default
    private boolean active = true;
    
    private String ruleType;
    private String ruleConfig;
}