package com.jw.holidayguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Defines the actual scheduling logic (when should this schedule run) using various rule types.
 * Rules are tied to specific schedule versions to preserve complete rule history for audit and debugging.
 */
@Entity
@Table(name = "rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "version_id", nullable = false, unique = true)
    private Long versionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private Version version;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Column(name = "rule_config", columnDefinition = "TEXT")
    private String ruleConfig;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (effectiveFrom == null) effectiveFrom = LocalDate.now();
    }

    /**
     * Rule types and their expected ruleConfig format:
     * 
     * WEEKDAYS_ONLY: ruleConfig = null or empty
     *   - Runs Monday-Friday, excludes weekends
     * 
     * CRON_EXPRESSION: ruleConfig = "0 0 9 * * MON-FRI"
     *   - Standard cron expression for scheduling
     *   - Example: "0 0 9 * * MON-FRI" = 9 AM weekdays
     * 
     * CUSTOM_DATES: ruleConfig = JSON array of dates
     *   - Example: ["2024-01-15", "2024-02-15", "2024-03-15"]
     *   - Explicit list of dates when schedule should run
     * 
     * MONTHLY_PATTERN: ruleConfig = JSON object with pattern
     *   - Example: {"dayOfMonth": 15, "skipWeekends": true}
     *   - Example: {"dayOfWeek": "FRIDAY", "weekOfMonth": "LAST"}
     * 
     * AVOID_US_FEDERAL_HOLIDAYS: ruleConfig = null or empty
     *  - Runs on weekdays, but skips all official US federal holidays
     */
    public enum RuleType {
        WEEKDAYS_ONLY,
        US_FEDERAL_RESERVE_BUSINESS_DAYS,
        CRON_EXPRESSION,
        ALL_DAYS
    }
}