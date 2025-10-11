package com.jw.holidayguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Each time a schedule, its rule, or overrides is modified, the updated set is saved as a new version.
 * Therefore, audit records will have a history of how a "shouldRun()" Calendar was configured.
 */
@Entity
@Table(name = "version")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (effectiveFrom == null) effectiveFrom = Instant.now();
    }

    /**
     * Creates unsaved version from existing schedule
     *
     * </li>
     *   <li>Even a "new" schedule needs a version to track when it was created</li>
     *   <li>The domain model handles version creation - database handles activation</li>
     * </ul>
     *
     * @param schedule The schedule this version belongs to
     * @return A new Version instance representing version 1
     */
    public static VersionBuilder builderFrom(Schedule schedule) {
        return builderFrom(schedule.getId());
    }

    /**
     * Creates unsaved version from existing schedule
     * This one primarily used in domain testing
     *
     * </li>
     *   <li>Even a "new" schedule needs a version to track when it was created</li>
     *   <li>The domain model handles version creation - database handles activation</li>
     * </ul>
     *
     * @param scheduleId The primary key of the schedule
     * @return A new Version instance representing version 1
     */
    public static VersionBuilder builderFrom(Long scheduleId) {
        return Version.builder()
                .scheduleId(scheduleId)
                // .version(1) // officially null until db save, but it's v1
                .active(false) // Starts inactive - database layer activates after save
                .effectiveFrom(Instant.now());
    }
}