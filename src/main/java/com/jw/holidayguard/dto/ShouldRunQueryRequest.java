package com.jw.holidayguard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShouldRunQueryRequest {
    
    @NotNull(message = "Query date is required")
    private LocalDate queryDate;
    
    private String clientIdentifier;
}