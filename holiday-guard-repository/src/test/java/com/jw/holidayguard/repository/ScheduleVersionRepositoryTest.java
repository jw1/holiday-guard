package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.util.ScheduleTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ScheduleVersionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleVersionRepository scheduleVersionRepository;

    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        testSchedule = ScheduleTestDataFactory.createPayrollSchedule();
        testSchedule = entityManager.persistAndFlush(testSchedule);
    }

    @Test
    void shouldFindActiveVersionForSchedule() {
        // Given: A schedule with multiple versions, only one active
        ScheduleVersion inactiveVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), false);
        ScheduleVersion activeVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), true);
        
        entityManager.persistAndFlush(inactiveVersion);
        entityManager.persistAndFlush(activeVersion);

        // When: Finding active version
        Optional<ScheduleVersion> result = scheduleVersionRepository.findByScheduleIdAndActiveTrue(testSchedule.getId());

        // Then: Should return the active version
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeVersion.getId());
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void shouldReturnAllVersionsOrderedByCreatedAt() {
        // Given: Multiple versions created at different times
        ScheduleVersion oldVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), false);
        oldVersion.setCreatedAt(Instant.now().minusSeconds(3600)); // 1 hour ago
        
        ScheduleVersion newVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), true);
        newVersion.setCreatedAt(Instant.now());
        
        entityManager.persistAndFlush(oldVersion);
        entityManager.persistAndFlush(newVersion);

        // When: Getting all versions
        List<ScheduleVersion> versions = scheduleVersionRepository.findByScheduleIdOrderByCreatedAtDesc(testSchedule.getId());

        // Then: Should be ordered by created date descending
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getId()).isEqualTo(newVersion.getId());
        assertThat(versions.get(1).getId()).isEqualTo(oldVersion.getId());
    }

    @Test
    void shouldCheckIfActiveVersionExists() {
        // Given: A schedule with an active version
        ScheduleVersion activeVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), true);
        entityManager.persistAndFlush(activeVersion);

        // When: Checking if active version exists
        boolean exists = scheduleVersionRepository.existsByScheduleIdAndActiveTrue(testSchedule.getId());

        // Then: Should return true
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoActiveVersionExists() {
        // Given: A schedule with only inactive versions
        ScheduleVersion inactiveVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), false);
        entityManager.persistAndFlush(inactiveVersion);

        // When: Checking if active version exists
        boolean exists = scheduleVersionRepository.existsByScheduleIdAndActiveTrue(testSchedule.getId());

        // Then: Should return false
        assertThat(exists).isFalse();
    }
}