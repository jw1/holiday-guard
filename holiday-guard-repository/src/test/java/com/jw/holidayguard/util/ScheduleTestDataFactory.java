package com.jw.holidayguard.util;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Version;

import java.time.Instant;


public class ScheduleTestDataFactory {

    public static Schedule createPayrollSchedule() {
        return Schedule.builder()
                .name("Weekly Payroll")
                .description("Standard weekly payroll processing")
                .country("US")
                .active(true)
                .build();
    }

    public static Version createScheduleVersion(Long scheduleId, boolean active) {
        return Version.builder()
                .scheduleId(scheduleId)
                .effectiveFrom(Instant.now())
                .active(active)
                .build();
    }
}
