package com.jw.holidayguard.dto;

import com.jw.holidayguard.domain.RunStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.Map;

/**
 * DTO representing a calendar month for a single schedule.
 * Maps day-of-month (1-31) to run status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMonthDto {
    private YearMonth yearMonth;
    private Map<Integer, RunStatus> days;
}
