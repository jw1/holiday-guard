package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Version;
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
class VersionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VersionRepository versionRepository;

    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        testSchedule = ScheduleTestDataFactory.createPayrollSchedule();
        testSchedule = entityManager.persistAndFlush(testSchedule);
    }

    @Test
    void shouldFindActiveVersionForSchedule() {
        // given - A schedule with multiple versions, only one active
        Version inactiveVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), false);
        Version activeVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), true);
        
        entityManager.persistAndFlush(inactiveVersion);
        entityManager.persistAndFlush(activeVersion);

        // when - Finding active version
        Optional<Version> result = versionRepository.findByScheduleIdAndActiveTrue(testSchedule.getId());

        // then - Should return the active version
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeVersion.getId());
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void shouldReturnAllVersionsOrderedByCreatedAt() {
        // given - Multiple versions created at different times
        Version oldVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), false);
        oldVersion.setCreatedAt(Instant.now().minusSeconds(3600)); // 1 hour ago
        
        Version newVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), true);
        newVersion.setCreatedAt(Instant.now());
        
        entityManager.persistAndFlush(oldVersion);
        entityManager.persistAndFlush(newVersion);

        // when - Getting all versions
        List<Version> versions = versionRepository.findByScheduleIdOrderByCreatedAtDesc(testSchedule.getId());

        // then - Should be ordered by created date descending
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getId()).isEqualTo(newVersion.getId());
        assertThat(versions.get(1).getId()).isEqualTo(oldVersion.getId());
    }

    @Test
    void shouldCheckIfActiveVersionExists() {
        // given - A schedule with an active version
        Version activeVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), true);
        entityManager.persistAndFlush(activeVersion);

        // when - Checking if active version exists
        boolean exists = versionRepository.existsByScheduleIdAndActiveTrue(testSchedule.getId());

        // then - Should return true
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoActiveVersionExists() {
        // given - A schedule with only inactive versions
        Version inactiveVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), false);
        entityManager.persistAndFlush(inactiveVersion);

        // when - Checking if active version exists
        boolean exists = versionRepository.existsByScheduleIdAndActiveTrue(testSchedule.getId());

        // then - Should return false
        assertThat(exists).isFalse();
    }
}