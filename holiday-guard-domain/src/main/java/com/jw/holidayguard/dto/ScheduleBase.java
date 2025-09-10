package com.jw.holidayguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ScheduleBase {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Country is required")
    private String country = "US";

    private boolean active = true;
}