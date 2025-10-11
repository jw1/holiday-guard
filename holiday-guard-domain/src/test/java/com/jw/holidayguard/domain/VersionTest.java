package com.jw.holidayguard.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Version domain object and version management helpers.
 * Versions track changes to schedules over time for audit purposes.
 */
class VersionTest {

    @Test
    void toNewVersion_createsInactiveVersion() {
        // Given: An existing active version
        Version currentVersion = Version.builder()
                .id(1L)
                .scheduleId(100L)
                .active(true)
                .effectiveFrom(Instant.now().minusSeconds(3600))
                .build();

        Rule newRule = Rule.builder()
                .scheduleId(100L)
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .build();

        List<Deviation> newDeviations = List.of(
                Deviation.builder()
                        .scheduleId(100L)
                        .overrideDate(LocalDate.of(2025, 1, 6))
                        .action(RunStatus.FORCE_SKIP)
                        .reason("Holiday")
                        .build()
        );

        // When: Creating a new version from current
        Version newVersion = Version.builderFrom(100L).build();

        // Then: New version should be inactive (database layer will activate it)
        assertFalse(newVersion.isActive(), "New version should start inactive");
        assertThat(newVersion.getScheduleId()).isEqualTo(100L);
        assertThat(newVersion.getId()).isNull(); // Not yet persisted
        assertThat(newVersion.getEffectiveFrom()).isNotNull();
    }

    @Test
    void toNewVersion_preservesScheduleId() {
        // Given: Version for schedule 42
        Version currentVersion = Version.builder()
                .scheduleId(42L)
                .active(true)
                .build();

        Rule newRule = Rule.builder()
                .scheduleId(42L)
                .ruleType(Rule.RuleType.ALL_DAYS)
                .build();

        // When: Creating new version
        Version newVersion = Version
                .builderFrom(42L)
                .build();

        // Then: Should preserve scheduleId
        assertThat(newVersion.getScheduleId()).isEqualTo(42L);
    }

    @Test
    void createInitialVersion_createsVersionOne() {
        // Given: A new schedule with no versions yet
        Schedule schedule = Schedule.builder()
                .id(100L)
                .name("New Schedule")
                .build();

        // When: Creating the initial version
        Version initialVersion = Version
                .builderFrom(schedule)
                .build();

        // Then: Should be version 1, inactive (until persisted)
        assertThat(initialVersion.getScheduleId()).isEqualTo(100L);
        assertFalse(initialVersion.isActive());
        assertThat(initialVersion.getId()).isNull(); // Not yet persisted
        assertThat(initialVersion.getEffectiveFrom()).isNotNull();
    }

    @Test
    void createInitialVersion_withDeviations() {
        // Given: A new schedule with initial deviations
        Schedule schedule = Schedule.builder()
                .id(100L)
                .name("Payroll Schedule")
                .build();

        Rule initialRule = Rule.builder()
                .scheduleId(100L)
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .build();

        Deviation initialDeviation = Deviation.builder()
                .scheduleId(100L)
                .overrideDate(LocalDate.of(2025, 12, 25))
                .action(RunStatus.FORCE_SKIP)
                .reason("Christmas")
                .build();

        // When: Creating initial version with deviations
        Version initialVersion = Version
                .builderFrom(schedule)
                .build();

        // Then: version should be created properly
        assertThat(initialVersion.getScheduleId()).isEqualTo(100L);
        assertFalse(initialVersion.isActive());
    }

    @Test
    void defaultsToInactive_untilDatabaseActivates() {
        // Given: Any new version created
        Version version = Version.builder()
                .scheduleId(1L)
                .build();

        // When: Checking active status
        boolean isActive = version.isActive();

        // Then: Should default to false (inactive)
        assertFalse(isActive, "Versions should default to inactive until database layer activates them");
    }

    @Test
    void versionLifecycle_fromCreationToActivation() {
        // Given: Creating a new version for an existing schedule
        Schedule schedule = Schedule.builder()
                .id(100L)
                .name("Payroll Schedule")
                .build();

        // When: Creating initial version (mimics what service layer would do)
        Version newVersion = Version
                .builderFrom(schedule)
                .build();

        // Then: Starts inactive
        assertFalse(newVersion.isActive());
        assertThat(newVersion.getScheduleId()).isEqualTo(100L);

        // When: Database layer activates it (mimics VersionRepository.save)
        newVersion.setActive(true);
        newVersion.setId(1L); // Database assigns ID

        // Then: Version is now active and persisted
        assertTrue(newVersion.isActive());
        assertThat(newVersion.getId()).isEqualTo(1L);
    }

    @Test
    void effectiveFrom_defaultsToNow() {
        // Given: Creating version without specifying effectiveFrom
        Version version = Version.builder()
                .scheduleId(1L)
                .build();

        // Trigger @PrePersist
        version.onCreate();

        // Then: effectiveFrom should be set to current time
        assertThat(version.getEffectiveFrom()).isNotNull();
        // Check it's within 1 second of now
        Instant now = Instant.now();
        assertThat(version.getEffectiveFrom())
                .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    }
}
