package com.jw.holidayguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class CreateScheduleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Country is required")
    private String country = "US";

    private boolean active = true;
    
    private String ruleType;
    private String ruleConfig;
}