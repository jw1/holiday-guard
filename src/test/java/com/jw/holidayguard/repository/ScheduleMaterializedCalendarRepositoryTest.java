package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleMaterializedCalendar;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.util.ScheduleTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ScheduleMaterializedCalendarRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleMaterializedCalendarRepository materializedCalendarRepository;

    private Schedule testSchedule;
    private ScheduleVersion testVersion;

    @BeforeEach
    void setUp() {
        testSchedule = ScheduleTestDataFactory.createPayrollSchedule();
        testSchedule = entityManager.persistAndFlush(testSchedule);
        
        testVersion = ScheduleTestDataFactory.createScheduleVersion(testSchedule.getId(), true);
        testVersion = entityManager.persistAndFlush(testVersion);
    }

    @Test
    void shouldFindCalendarEntryForSpecificDate() {
        // Given: A calendar entry for a specific date
        LocalDate testDate = LocalDate.of(2024, 3, 15);
        ScheduleMaterializedCalendar entry = ScheduleTestDataFactory.createCalendarEntry(
                testSchedule.getId(), testVersion.getId(), testDate);
        entityManager.persistAndFlush(entry);

        // When: Finding entry for that date
        Optional<ScheduleMaterializedCalendar> result = materializedCalendarRepository
                .findByScheduleIdAndVersionIdAndOccursOn(testSchedule.getId(), testVersion.getId(), testDate);

        // Then: Should find the entry
        assertThat(result).isPresent();
        assertThat(result.get().getOccursOn()).isEqualTo(testDate);
    }

    @Test
    void shouldFindEntriesInDateRange() {
        // Given: Multiple calendar entries across different dates
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate midDate = LocalDate.of(2024, 3, 15);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        LocalDate outsideDate = LocalDate.of(2024, 4, 1);

        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), startDate));
        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), midDate));
        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), endDate));
        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), outsideDate));

        // When: Finding entries in March 2024
        List<ScheduleMaterializedCalendar> results = materializedCalendarRepository
                .findByScheduleIdAndVersionIdAndOccursOnBetween(
                        testSchedule.getId(), testVersion.getId(), 
                        LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));

        // Then: Should find only March entries
        assertThat(results).hasSize(3);
        assertThat(results).extracting(ScheduleMaterializedCalendar::getOccursOn)
                .containsExactlyInAnyOrder(startDate, midDate, endDate);
    }

    @Test
    void shouldCountOccurrencesInDateRange() {
        // Given: Multiple calendar entries
        LocalDate date1 = LocalDate.of(2024, 3, 1);
        LocalDate date2 = LocalDate.of(2024, 3, 15);
        LocalDate date3 = LocalDate.of(2024, 3, 31);

        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), date1));
        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), date2));
        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), date3));

        // When: Counting occurrences in March
        long count = materializedCalendarRepository.countOccurrencesInDateRange(
                testSchedule.getId(), testVersion.getId(),
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));

        // Then: Should count all March entries
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldFindNextNOccurrences() {
        // Given: Multiple future calendar entries
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfter = today.plusDays(2);
        LocalDate weekLater = today.plusDays(7);

        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), tomorrow));
        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), dayAfter));
        entityManager.persistAndFlush(ScheduleTestDataFactory.createCalendarEntry(testSchedule.getId(), testVersion.getId(), weekLater));

        // When: Finding next 2 occurrences
        List<ScheduleMaterializedCalendar> results = materializedCalendarRepository
                .findNextNOccurrences(testSchedule.getId(), testVersion.getId(), today, 2);

        // Then: Should return first 2 future dates in order
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getOccursOn()).isEqualTo(tomorrow);
        assertThat(results.get(1).getOccursOn()).isEqualTo(dayAfter);
    }
}