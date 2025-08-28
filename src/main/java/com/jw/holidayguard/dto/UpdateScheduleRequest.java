package com.jw.holidayguard.dto;

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
    
    private String name;
    private String description;
    private String country;
    
    // Note: active field updates not supported via this endpoint
    // Use dedicated activate/deactivate endpoints for state changes
    
    // NO ID field - ID comes from path parameter
}