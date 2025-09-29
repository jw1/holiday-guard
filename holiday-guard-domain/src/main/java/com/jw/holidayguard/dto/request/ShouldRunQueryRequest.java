package com.jw.holidayguard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShouldRunQueryRequest {

    private LocalDate queryDate = LocalDate.now();
    private String clientIdentifier;

    public ShouldRunQueryRequest(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
    
    /**
     * Gets the query date, defaulting to today if null.
     */
    public LocalDate getQueryDate() {
        return null != queryDate ? queryDate : LocalDate.now();
    }
}