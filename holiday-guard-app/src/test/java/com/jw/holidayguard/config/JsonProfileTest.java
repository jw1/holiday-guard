package com.jw.holidayguard.config;

import com.jw.holidayguard.repository.DataProvider;
import com.jw.holidayguard.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    private DataProvider provider;

    @Autowired
    private ScheduleRepository repo;

    @Test
    void shouldLoadJsonDataProvider() {
        assertThat(provider).isNotNull();
        assertThat(provider.getProviderName()).isEqualTo("JSON");
        assertThat(provider.supportsManagement()).isFalse();
    }

    @Test
    void shouldLoadSchedulesFromJson() {
        var schedules = repo.findAll();
        assertThat(schedules).isNotEmpty();
        assertThat(schedules).hasSize(4); // From data.json
    }

    @Test
    void shouldFindScheduleByName() {
        var schedule = repo.findByName("US Federal Holidays");
        assertThat(schedule).isPresent();
        assertThat(schedule.get().getCountry()).isEqualTo("US");
        assertThat(schedule.get().isActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenSaving() {
        var schedule = repo.findById(1L).get();
        assertThat(schedule).isNotNull();

        // Attempting to save should throw UnsupportedOperationException
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> repo.save(schedule),
                "JSON repository is read-only. Use H2 profile for CRUD operations."
        );
    }
}
