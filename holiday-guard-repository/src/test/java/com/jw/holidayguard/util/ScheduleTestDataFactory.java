package com.jw.holidayguard.util;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleMaterializedCalendar;
import com.jw.holidayguard.domain.ScheduleVersion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ScheduleTestDataFactory {

    public static Schedule createPayrollSchedule() {
        return Schedule.builder()
                .name("Weekly Payroll")
                .description("Standard weekly payroll processing")
                .country("US")
                .active(true)
                .build();
    }

    public static ScheduleVersion createScheduleVersion(UUID scheduleId, boolean active) {
        return ScheduleVersion.builder()
                .scheduleId(scheduleId)
                .effectiveFrom(Instant.now())
                .active(active)
                .build();
    }

    public static ScheduleMaterializedCalendar createCalendarEntry(UUID scheduleId, UUID versionId, LocalDate date) {
        return ScheduleMaterializedCalendar.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .occursOn(date)
                .status(ScheduleMaterializedCalendar.OccurrenceStatus.SCHEDULED)
                .build();
    }
}
