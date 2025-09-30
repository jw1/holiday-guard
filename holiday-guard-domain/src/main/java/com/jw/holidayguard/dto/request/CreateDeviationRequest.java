package com.jw.holidayguard.dto.request;

import com.jw.holidayguard.domain.Deviation;
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
public class CreateDeviationRequest {
    
    @NotNull(message = "Deviation date is required")
    private LocalDate deviationDate;
    
    @NotNull(message = "Deviation action is required")
    private Deviation.Action action;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must be no more than 500 characters")
    private String reason;
    
    private String createdBy;
    
    private LocalDate expiresAt;
}