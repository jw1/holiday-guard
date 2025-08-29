package com.jw.holidayguard.dto;

import com.jw.holidayguard.domain.ScheduleOverride;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleOverrideRequest {
    
    @NotNull(message = "Override date is required")
    private LocalDate overrideDate;
    
    @NotNull(message = "Override action is required")
    private ScheduleOverride.OverrideAction action;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must be no more than 500 characters")
    private String reason;
    
    private String createdBy;
    
    private LocalDate expiresAt;
}