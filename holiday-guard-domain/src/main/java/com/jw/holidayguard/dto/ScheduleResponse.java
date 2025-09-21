package com.jw.holidayguard.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleResponse extends ScheduleBase {

    // Inherits all fields from ScheduleBase:
    // - String name (validated)
    // - String description  
    // - String country (defaults to "US")
    // - boolean active (defaults to true)
    
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;

    private String ruleType;
    private String ruleConfig;
}