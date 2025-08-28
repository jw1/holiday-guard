package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Schedule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ScheduleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleRepository repo;

    @Test
    void insertAndSelect() {

        // given - valid schedule
        var toInsert = Schedule.builder()
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        // when - schedule is saved
        var inserted = repo.save(toInsert);

        // then - fields set on save are set
        assertThat(inserted.getId()).isNotNull();
        assertThat(inserted.getCreatedAt()).isNotNull();
        assertThat(inserted.getUpdatedAt()).isNotNull();

        // then - selected object matches inserted
        Optional<Schedule> selected = repo.findById(inserted.getId());
        assertThat(selected).isPresent();
        assertThat(inserted).isEqualTo(selected.get());
    }

    @Test
    void findByName() {

        // given - schedule persisted in db
        var toInsert = Schedule.builder()
                .name("Bank Holidays")
                .description("US bank holidays")
                .build();
        var inserted = entityManager.persistAndFlush(toInsert);

        // when - selected by name
        Optional<Schedule> found = repo.findByName("Bank Holidays");

        // then - it is found
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(inserted);
    }

    @Test
    void findByNameMiss() {
        assertThat(repo.findByName("missing schedule")).isEmpty();
    }

    @Test
    void findActive() {

        // given - 2 active schedules, 1 inactive in db
        var toInsert = List.of(
                Schedule.builder().name("Active 1").build(),
                Schedule.builder().name("Active 2").build(),
                Schedule.builder().name("Inactive").active(false).build()
        );
        toInsert.forEach(entityManager::persistAndFlush);

        // when - active schedules are looked up
        var activeSchedules = repo.findByActiveTrue();

        // then - only 2 are found
        assertThat(activeSchedules).hasSize(2);
        assertThat(activeSchedules).allMatch(Schedule::isActive);
    }

    @Test
    void findByCountry() {

        // given - 2 schedules (1 US, 1 CA)
        var toInsert = List.of(
                Schedule.builder().name("US Schedule").build(),
                Schedule.builder().name("CA Schedule").country("CA").build()
        );
        toInsert.forEach(entityManager::persistAndFlush);

        // when - selected by country
        var usSchedules = repo.findByCountry("US");
        var caSchedules = repo.findByCountry("CA");

        // then - each are found
        assertThat(usSchedules).hasSize(1);
        assertThat(usSchedules.getFirst().getName()).isEqualTo("US Schedule");
        assertThat(caSchedules).hasSize(1);
        assertThat(caSchedules.getFirst().getName()).isEqualTo("CA Schedule");
    }

    @Test
    void rejectDuplicateNames() {

        // given - 2 schedules with same name
        entityManager.persistAndFlush(Schedule.builder()
                .name("Duplicate")
                .build());
        
        var duplicate = Schedule.builder()
                .name("Duplicate")
                .build();

        // when - 2nd one is saved an exception is thrown
        assertThatThrownBy(() -> repo.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findActiveByCountry() {

        // given - schedules in db with assorted country, active values
        var toInsert = List.of(
                Schedule.builder().name("Active US").country("US").build(),
                Schedule.builder().name("Inactive US").country("US").active(false).build(),
                Schedule.builder().name("Active CA").country("CA").build()
        );
        toInsert.forEach(entityManager::persistAndFlush);

        // when - retrieved from db
        var activeUSSchedules = repo.findByCountryAndActiveTrue("US");
        var activeCASchedules = repo.findByCountryAndActiveTrue("CA");

        // then - only active found for each country
        assertThat(activeUSSchedules).hasSize(1);
        assertThat(activeUSSchedules.getFirst().getName()).isEqualTo("Active US");
        assertThat(activeCASchedules).hasSize(1);
        assertThat(activeCASchedules.getFirst().getName()).isEqualTo("Active CA");
    }
}