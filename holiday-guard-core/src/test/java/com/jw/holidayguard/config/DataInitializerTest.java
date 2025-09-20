package com.jw.holidayguard.config;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { DataInitializer.class })
@ActiveProfiles("local")
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = ScheduleRepository.class)
@EntityScan(basePackageClasses = Schedule.class)
@AutoConfigureTestDatabase
class DataInitializerTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void shouldLoadSampleSchedulesOnLocalProfile() {
        List<Schedule> schedules = scheduleRepository.findAll();

        assertThat(schedules).hasSize(4);
        assertThat(schedules)
                .extracting(Schedule::getName)
                .containsExactlyInAnyOrder(
                        "US Federal Holidays",
                        "UK Bank Holidays",
                        "Canadian Public Holidays",
                        "Australian Public Holidays"
                );
    }
}
