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
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.jw.holidayguard.domain.Rule.RuleType.US_FEDERAL_RESERVE_BUSINESS_DAYS;
import static com.jw.holidayguard.domain.Rule.RuleType.WEEKDAYS_ONLY;
import static com.jw.holidayguard.domain.RunStatus.FORCE_RUN;
import static com.jw.holidayguard.domain.RunStatus.FORCE_SKIP;
import static java.time.DayOfWeek.MONDAY;

/**
 * Preloads data into a new database when the application is started up under the
 * demo profile
 */
@Component
@Profile("demo")
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
                        .deviationDate(LocalDate.now().with(TemporalAdjusters.next(MONDAY)))
                        .action(FORCE_SKIP)
                        .reason("Team Offsite Next Monday")
                        .build(),
                Deviation.builder()
                        .scheduleId(schedule.getId())
                        .versionId(version.getId())
                        .deviationDate(LocalDate.of(2025, 12, 31))
                        .action(FORCE_RUN)
                        .reason("New Year's Bonus Payroll Run")
                        .build()));


        // just weekdays with a few deviations
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
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 1, 1))
                        .action(FORCE_SKIP).reason("New Year's Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 4, 3))
                        .action(FORCE_SKIP).reason("Good Friday").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 4, 6))
                        .action(FORCE_SKIP).reason("Easter Monday").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 5, 4))
                        .action(FORCE_SKIP).reason("Early May Bank Holiday").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 5, 25))
                        .action(FORCE_SKIP).reason("Spring Bank Holiday").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 8, 31))
                        .action(FORCE_SKIP).reason("Summer Bank Holiday").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 12, 25))
                        .action(FORCE_SKIP).reason("Christmas Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 12, 28))
                        .action(FORCE_SKIP).reason("Boxing Day").build()
        ));


        // Canadian Public Holidays
        schedule = scheduleRepository.save(Schedule.builder()
                .name("Canadian Public Holidays")
                .description("Holidays in Canada, but not US")
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
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 1, 1))
                        .action(FORCE_SKIP).reason("New Year's Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 4, 3))
                        .action(FORCE_SKIP).reason("Good Friday").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 5, 18))
                        .action(FORCE_SKIP).reason("Victoria Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 7, 1))
                        .action(FORCE_SKIP).reason("Canada Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 9, 7))
                        .action(FORCE_SKIP).reason("Labour Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 9, 30))
                        .action(FORCE_SKIP).reason("National Day for Truth and Reconciliation").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 10, 12))
                        .action(FORCE_SKIP).reason("Thanksgiving Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 11, 11))
                        .action(FORCE_SKIP).reason("Remembrance Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 12, 25))
                        .action(FORCE_SKIP).reason("Christmas Day").build(),
                Deviation.builderFrom(schedule, version)
                        .deviationDate(LocalDate.of(2026, 12, 28))
                        .action(FORCE_SKIP).reason("Boxing Day").build()
        ));

        // Australian Public Holidays
        schedule = scheduleRepository.save(Schedule.builder()
                .name("Australian Public Holidays")
                .description("National public holidays in Australia")
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

        overrideRepository.saveAll(List.of(
                Deviation.builderFrom(schedule, version).deviationDate(LocalDate.of(2026, 1, 1))
                        .action(FORCE_SKIP).reason("New Year's Day").build(),
                Deviation.builderFrom(schedule, version).deviationDate(LocalDate.of(2026, 1, 26))
                        .action(FORCE_SKIP).reason("Australia Day").build(),
                Deviation.builderFrom(schedule, version).deviationDate(LocalDate.of(2026, 4, 3))
                        .action(FORCE_SKIP).reason("Good Friday").build(),
                Deviation.builderFrom(schedule, version).deviationDate(LocalDate.of(2026, 4, 6))
                        .action(FORCE_SKIP).reason("Easter Monday").build(),
                Deviation.builderFrom(schedule, version).deviationDate(LocalDate.of(2026, 4, 25))
                        .action(FORCE_SKIP).reason("Anzac Day").build(),
                Deviation.builderFrom(schedule, version).deviationDate(LocalDate.of(2026, 12, 25))
                        .action(FORCE_SKIP).reason("Christmas Day").build(),
                Deviation.builderFrom(schedule, version).deviationDate(LocalDate.of(2026, 12, 28))
                        .action(FORCE_SKIP).reason("Boxing Day").build()
        ));
    }
}