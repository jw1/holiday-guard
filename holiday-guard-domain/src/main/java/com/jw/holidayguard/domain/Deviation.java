package com.jw.holidayguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Alterations which modify schedule rules for specific dates.
 * Deviations are tied to specific schedule versions to maintain complete audit trail of what changed between versions.
 *
 * <p>Deviations always represent "forced" run statuses (FORCE_RUN or FORCE_SKIP) that override
 * the base schedule rule for specific dates.
 */
@Entity
@Table(name = "deviation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deviation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private Version version;

    @Column(name = "override_date", nullable = false)
    private LocalDate deviationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private RunStatus action;

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
        if (createdAt == null) createdAt = Instant.now();
        if (createdBy == null) createdBy = "system";
        validateAction();
    }

    @PreUpdate
    protected void onUpdate() {
        validateAction();
    }

    public boolean shouldRun() {
        return RunStatus.FORCE_RUN == this.getAction();
    }

    /**
     * Validates that the action is one of the valid deviation types (FORCE_RUN or FORCE_SKIP).
     * Deviations cannot have RUN or SKIP status - those come from the base rule.
     */
    private void validateAction() {
        if (action != null && action != RunStatus.FORCE_RUN && action != RunStatus.FORCE_SKIP) {
            throw new IllegalStateException(
                "Deviation action must be FORCE_RUN or FORCE_SKIP, but was: " + action +
                ". Use FORCE_RUN to override rule and run, or FORCE_SKIP to override rule and skip."
            );
        }
    }

    public static Deviation.DeviationBuilder builderFrom(Schedule schedule, Version version) {
        return builder()
                .deviationDate(null)
                .action(null)
                .reason(null)
                .scheduleId(schedule.getId())
                .versionId(version.getId());
    }
}