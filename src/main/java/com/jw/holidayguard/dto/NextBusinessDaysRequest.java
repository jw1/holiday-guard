package com.jw.holidayguard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextBusinessDaysRequest {
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 365, message = "Count cannot exceed 365")
    @NotNull(message = "Count is required")
    private Integer count;
}