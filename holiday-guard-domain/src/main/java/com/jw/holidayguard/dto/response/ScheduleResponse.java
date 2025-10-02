package com.jw.holidayguard.dto.response;

import lombok.*;

import java.time.Instant;

@Data
public class ScheduleResponse {

    // Fields from former ScheduleBase
    private String name;
    private String description;
    private String country;
    private boolean active;

    // Own fields
    private Long id;
    private Instant createdAt;
    private Instant updatedAt;

    private String ruleType;
    private String ruleConfig;
}