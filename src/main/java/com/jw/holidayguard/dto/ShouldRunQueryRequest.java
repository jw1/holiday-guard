package com.jw.holidayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShouldRunQueryRequest {
    
    /**
     * Query date - defaults to today if not provided.
     * This makes the API more user-friendly for common cases.
     */
    private LocalDate queryDate;
    
    private String clientIdentifier;
    
    /**
     * Gets the query date, defaulting to today if null.
     */
    public LocalDate getQueryDate() {
        return queryDate != null ? queryDate : LocalDate.now();
    }
}