package com.jw.holidayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextBusinessDaysResponse {
    
    private UUID scheduleId;
    private LocalDate startDate;
    private int requestedCount;
    private List<LocalDate> businessDays;
    private UUID versionId;
}