package com.jw.holidayguard.config;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.repository.ScheduleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
public class DataInitializer implements ApplicationRunner {

    private final ScheduleRepository scheduleRepository;

    public DataInitializer(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (scheduleRepository.count() == 0) {
            Schedule schedule1 = Schedule.builder()
                    .name("US Federal Holidays")
                    .description("Standard US federal holidays")
                    .country("US")
                    .active(true)
                    .build();

            Schedule schedule2 = Schedule.builder()
                    .name("UK Bank Holidays")
                    .description("Official bank holidays in the United Kingdom")
                    .country("UK")
                    .active(true)
                    .build();

            Schedule schedule3 = Schedule.builder()
                    .name("Canadian Public Holidays")
                    .description("Statutory holidays for Canada")
                    .country("CA")
                    .active(false)
                    .build();

            Schedule schedule4 = Schedule.builder()
                    .name("Australian Public Holidays")
                    .description("Public holidays across Australia")
                    .country("AU")
                    .active(true)
                    .build();

            scheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3, schedule4));
        }
    }
}
