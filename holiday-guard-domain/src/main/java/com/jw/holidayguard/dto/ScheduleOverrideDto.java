package com.jw.holidayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleOverrideDto {
    private LocalDate date;
    private String type;
}
