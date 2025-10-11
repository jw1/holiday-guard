package com.jw.holidayguard.config;

import com.jw.holidayguard.repository.DataProvider;
import com.jw.holidayguard.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for JSON profile.
 * Verifies that the JSON repository loads data correctly and management is disabled.
 */
@SpringBootTest
@ActiveProfiles("json")
class JsonProfileTest {

    @Autowired
    private DataProvider dataProvider;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void shouldLoadJsonDataProvider() {
        assertThat(dataProvider).isNotNull();
        assertThat(dataProvider.getProviderName()).isEqualTo("JSON");
        assertThat(dataProvider.supportsManagement()).isFalse();
    }

    @Test
    void shouldLoadSchedulesFromJson() {
        var schedules = scheduleRepository.findAll();
        assertThat(schedules).isNotEmpty();
        assertThat(schedules).hasSize(4); // From data.json
    }

    @Test
    void shouldFindScheduleByName() {
        var schedule = scheduleRepository.findByName("US Federal Holidays");
        assertThat(schedule).isPresent();
        assertThat(schedule.get().getCountry()).isEqualTo("US");
        assertThat(schedule.get().isActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenSaving() {
        var schedule = scheduleRepository.findById(1L).get();
        assertThat(schedule).isNotNull();

        // Attempting to save should throw UnsupportedOperationException
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> scheduleRepository.save(schedule),
                "JSON repository is read-only. Use H2 profile for CRUD operations."
        );
    }
}
