package com.jw.holidayguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Permanent audit trail that records every "should I run today?" query and the answer given.
 * Includes version information for debugging scenarios. Used for compliance, debugging, and business analysis.
 * These records are never deleted.
 */
@Entity
@Table(name = "schedule_query_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleQueryLog {

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

    @Column(name = "query_date", nullable = false)
    private LocalDate queryDate;

    @Column(name = "should_run_result", nullable = false)
    private boolean shouldRunResult;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "override_applied", nullable = false)
    @Builder.Default
    private boolean overrideApplied = false;

    @Column(name = "queried_at", nullable = false)
    private Instant queriedAt;

    @Column(name = "client_identifier")
    private String clientIdentifier;

    @PrePersist
    protected void onCreate() {
        if (queriedAt == null) {
            queriedAt = Instant.now();
        }
        if (clientIdentifier == null) {
            clientIdentifier = "unknown";
        }
    }
}