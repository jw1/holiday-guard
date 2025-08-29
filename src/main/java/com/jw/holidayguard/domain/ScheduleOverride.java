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
 * Stores exceptions that modify the standard schedule rules for specific dates.
 * Overrides are tied to specific schedule versions to maintain complete audit trail of what changed between versions.
 */
@Entity
@Table(name = "schedule_overrides")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleOverride {

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

    @Column(name = "override_date", nullable = false)
    private LocalDate overrideDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private OverrideAction action;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (createdBy == null) {
            createdBy = "system";
        }
    }

    public enum OverrideAction {
        SKIP,     // Don't run on this date (answer "no")
        FORCE_RUN // Run even if base schedule says don't run (answer "yes")
    }
}