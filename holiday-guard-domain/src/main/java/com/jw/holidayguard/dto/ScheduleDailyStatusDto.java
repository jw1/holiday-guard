package com.jw.holidayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDailyStatusDto {
    private UUID id;
    private String name;
    private boolean shouldRun;
    private String reason;
}
