package com.jw.holidayguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateScheduleRequest {
    
    // All fields optional for partial updates
    // null/missing values indicate "don't update this field"
    
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    private String country;
    
    // Note: active field updates not supported via this endpoint
    // Use dedicated activate/deactivate endpoints for state changes
    
    // NO ID field - ID comes from path parameter
}