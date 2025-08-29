package com.jw.holidayguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The materialized calendar - pre-computed dates when each schedule should run.
 * Contains only "should run = YES" dates; if no record exists for a date, the answer is "NO".
 * Generated from schedule rules and modified by overrides.
 */
@Entity
@Table(name = "schedule_materialized_calendar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleMaterializedCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private ScheduleVersion version;

    @Column(name = "occurs_on", nullable = false)
    private LocalDate occursOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OccurrenceStatus status = OccurrenceStatus.SCHEDULED;

    @Column(name = "override_id")
    private UUID overrideId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "override_id", insertable = false, updatable = false)
    private ScheduleOverride override;

    public enum OccurrenceStatus {
        SCHEDULED,  // Normal occurrence based on rules
        OVERRIDDEN, // Modified by an override
        COMPLETED   // Process has run for this date
    }
}