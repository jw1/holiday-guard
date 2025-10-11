package com.jw.holidayguard.config;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {DataInitializer.class})
@ActiveProfiles("h2")
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = ScheduleRepository.class)
@EntityScan(basePackageClasses = Schedule.class)
@AutoConfigureTestDatabase
public class DataInitializerIntegrationTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Test
    void shouldInitializeData() {
        // given
        long scheduleCount = scheduleRepository.count();
        long ruleCount = ruleRepository.count();

        // then
        assertThat(scheduleCount).isEqualTo(4);
        assertThat(ruleCount).isEqualTo(4);
    }

    @Test
    void shouldSetCorrectRuleForUsFederalHolidays() {
        // given
        Optional<Schedule> usScheduleOpt = scheduleRepository.findByName("US Federal Holidays");

        // then
        assertThat(usScheduleOpt).isPresent();
        Schedule usSchedule = usScheduleOpt.get();

        Optional<Rule> ruleOpt = ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(usSchedule.getId());
        assertThat(ruleOpt).isPresent();
        assertThat(ruleOpt.get().getRuleType()).isEqualTo(Rule.RuleType.US_FEDERAL_RESERVE_BUSINESS_DAYS);
    }
}
