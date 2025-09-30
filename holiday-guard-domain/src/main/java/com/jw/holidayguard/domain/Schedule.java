package com.jw.holidayguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * The application supports multiple schedules with different sets of holidays.
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
        // TODO: get user from security context
        var user = "api-user";
        createdAt = Instant.now();
        updatedAt = Instant.now();
        createdBy = user;
        updatedBy = user;
    }

    @PreUpdate
    protected void onUpdate() {
        // TODO: get user from security context
        var user = "api-user";
        updatedAt = Instant.now();
        updatedBy = user;
    }
}