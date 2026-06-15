package com.jw.holidayguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * app supports multiple schedules with different sets of holidays.
 * <p>
 * For example, one schedule could follow Federal Reserve holidays while
 * another might simply be "weekdays, not weekends".
 */
@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    @Builder.Default
    private String country = "US";

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    public Schedule(String name, String description) {
        this(name, description, "US");
    }

    public Schedule(String name, String description, String country) {
        this.name = name;
        this.description = description;
        this.country = country;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        // Service layer sets createdBy/updatedBy via CurrentUserService.
        // Fall back to "system" for programmatic creation (e.g. data initializer).
        if (createdBy == null) createdBy = "system";
        if (updatedBy == null) updatedBy = "system";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        // Service layer sets updatedBy before the transaction commits.
        // Fall back to "system" only if it was never set.
        if (updatedBy == null) updatedBy = "system";
    }
}