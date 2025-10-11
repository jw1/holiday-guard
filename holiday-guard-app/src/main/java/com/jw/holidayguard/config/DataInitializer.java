package com.jw.holidayguard.config;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.jw.holidayguard.domain.Rule.RuleType.US_FEDERAL_RESERVE_BUSINESS_DAYS;
import static com.jw.holidayguard.domain.Rule.RuleType.WEEKDAYS_ONLY;
import static com.jw.holidayguard.domain.RunStatus.FORCE_RUN;
import static com.jw.holidayguard.domain.RunStatus.FORCE_SKIP;

/**
 * Preloads data into a new database when the application is started up under the
 * h2 profile
 */
@Component
@Profile("h2")
public class DataInitializer implements ApplicationRunner {

    private final ScheduleRepository scheduleRepository;
    private final DeviationRepository overrideRepository;
    private final VersionRepository versionRepository;
    private final RuleRepository ruleRepository;

    public DataInitializer(ScheduleRepository scheduleRepository, DeviationRepository overrideRepository, VersionRepository versionRepository, RuleRepository ruleRepository) {
        this.scheduleRepository = scheduleRepository;
        this.overrideRepository = overrideRepository;
        this.versionRepository = versionRepository;
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        // preserve data between app runs, don't re-initialize
        if (scheduleRepository.count() > 0) return;


        // first demo schedule is US Federal Holidays w/ 2 deviations
        var schedule = scheduleRepository.save(Schedule.builder()
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .country("US")
                .active(true)
                .build());

        var version = versionRepository.save(Version
                .builderFrom(schedule)
                .active(true) // activate initial version
                .build());

        ruleRepository.save(Rule.builder()
                .scheduleId(schedule.getId())
                .ruleType(US_FEDERAL_RESERVE_BUSINESS_DAYS)
                .versionId(version.getId())
                .build());

        overrideRepository.saveAll(List.of(
                Deviation.builder()
                        .scheduleId(schedule.getId())
                        .versionId(version.getId())
                        .overrideDate(LocalDate.of(2025, 9, 15)) // TODO:  calculate "next Monday"
                        .action(FORCE_SKIP)
                        .reason("Team Offsite Next Monday")
                        .build(),
                Deviation.builder()
                        .scheduleId(schedule.getId())
                        .versionId(version.getId())
                        .overrideDate(LocalDate.of(2025, 12, 31))
                        .action(FORCE_RUN)
                        .reason("New Year's Bonus Payroll Run")
                        .build()));


        schedule = scheduleRepository.save(Schedule.builder()
                .name("UK Bank Holidays")
                .description("Official bank holidays in the United Kingdom")
                .country("UK")
                .active(true)
                .build());

        version = versionRepository.save(Version
                .builderFrom(schedule)
                .active(true)
                .build());

        ruleRepository.save(Rule.builder()
                .scheduleId(schedule.getId())
                .ruleType(WEEKDAYS_ONLY)
                .versionId(version.getId())
                .build());

        overrideRepository.saveAll(List.of(
                Deviation.builder()
                        .scheduleId(schedule.getId())
                        .versionId(version.getId())
                        .overrideDate(LocalDate.of(2025, 9, 22))
                        .action(FORCE_SKIP)
                        .reason("UK Team Event")
                        .build(),
                Deviation.builder()
                        .scheduleId(schedule.getId())
                        .versionId(version.getId())
                        .overrideDate(LocalDate.of(2025, 10, 27))
                        .action(FORCE_RUN)
                        .reason("End of Month Processing")
                        .build()));


        schedule = scheduleRepository.save(Schedule.builder()
                .name("Canadian Public Holidays")
                .description("Statutory holidays for Canada")
                .country("CA")
                .active(false)
                .build());

        version = versionRepository.save(Version
                .builderFrom(schedule)
                .active(true)
                .build());

        ruleRepository.save(Rule.builder()
                .scheduleId(schedule.getId())
                .ruleType(WEEKDAYS_ONLY)
                .versionId(version.getId())
                .build());

        overrideRepository.saveAll(List.of(
                Deviation.builder()
                        .scheduleId(schedule.getId())
                        .versionId(version.getId())
                        .overrideDate(LocalDate.of(2025, 9, 1))
                        .action(FORCE_SKIP)
                        .reason("Labour Day")
                        .build(),
                Deviation.builder()
                        .scheduleId(schedule.getId())
                        .versionId(version.getId())
                        .overrideDate(LocalDate.of(2025, 10, 13))
                        .action(FORCE_RUN)
                        .reason("Thanksgiving")
                        .build()));

        schedule = scheduleRepository.save(Schedule.builder()
                .name("Australian Public Holidays")
                .description("Public holidays across Australia")
                .country("AU")
                .active(true)
                .build());

        version = versionRepository.save(Version
                .builderFrom(schedule)
                .active(true)
                .build());

        ruleRepository.save(Rule.builder()
                .scheduleId(schedule.getId())
                .ruleType(WEEKDAYS_ONLY)
                .versionId(version.getId())
                .build());

        // No deviations for Australian schedule
    }
}