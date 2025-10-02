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

@Component
@Profile("local")
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
        if (scheduleRepository.count() == 0) {
            Schedule schedule1 = Schedule.builder()
                    .name("US Federal Holidays")
                    .description("Standard US federal holidays")
                    .country("US")
                    .active(true)
                    .build();
            scheduleRepository.save(schedule1);
            Version version1 = versionRepository.save(Version.builder().scheduleId(schedule1.getId()).active(true).build());
            ruleRepository.save(Rule.builder().scheduleId(schedule1.getId()).versionId(version1.getId()).ruleType(Rule.RuleType.US_FEDERAL_RESERVE_BUSINESS_DAYS).build());

            Schedule schedule2 = Schedule.builder()
                    .name("UK Bank Holidays")
                    .description("Official bank holidays in the United Kingdom")
                    .country("UK")
                    .active(true)
                    .build();
            scheduleRepository.save(schedule2);
            Version version2 = versionRepository.save(Version.builder().scheduleId(schedule2.getId()).active(true).build());
            ruleRepository.save(Rule.builder().scheduleId(schedule2.getId()).versionId(version2.getId()).ruleType(Rule.RuleType.WEEKDAYS_ONLY).build());

            Schedule schedule3 = Schedule.builder()
                    .name("Canadian Public Holidays")
                    .description("Statutory holidays for Canada")
                    .country("CA")
                    .active(false)
                    .build();
            scheduleRepository.save(schedule3);
            Version version3 = versionRepository.save(Version.builder().scheduleId(schedule3.getId()).active(true).build());
            ruleRepository.save(Rule.builder().scheduleId(schedule3.getId()).versionId(version3.getId()).ruleType(Rule.RuleType.WEEKDAYS_ONLY).build());

            Schedule schedule4 = Schedule.builder()
                    .name("Australian Public Holidays")
                    .description("Public holidays across Australia")
                    .country("AU")
                    .active(true)
                    .build();
            scheduleRepository.save(schedule4);
            Version version4 = versionRepository.save(Version.builder().scheduleId(schedule4.getId()).active(true).build());
            ruleRepository.save(Rule.builder().scheduleId(schedule4.getId()).versionId(version4.getId()).ruleType(Rule.RuleType.WEEKDAYS_ONLY).build());

            Deviation override1 = Deviation.builder()
                    .scheduleId(schedule1.getId())
                    .versionId(version1.getId())
                    .overrideDate(LocalDate.of(2025, 9, 15))
                    .action(Deviation.Action.SKIP)
                    .reason("Team Offsite")
                    .build();

            Deviation override2 = Deviation.builder()
                    .scheduleId(schedule1.getId())
                    .versionId(version1.getId())
                    .overrideDate(LocalDate.of(2025, 10, 31))
                    .action(Deviation.Action.FORCE_RUN)
                    .reason("Halloween Payroll Run")
                    .build();

            Deviation override3 = Deviation.builder()
                    .scheduleId(schedule2.getId())
                    .versionId(version2.getId())
                    .overrideDate(LocalDate.of(2025, 9, 22))
                    .action(Deviation.Action.SKIP)
                    .reason("UK Team Event")
                    .build();

            Deviation override4 = Deviation.builder()
                    .scheduleId(schedule2.getId())
                    .versionId(version2.getId())
                    .overrideDate(LocalDate.of(2025, 10, 27))
                    .action(Deviation.Action.FORCE_RUN)
                    .reason("End of Month Processing")
                    .build();

            Deviation override5 = Deviation.builder()
                    .scheduleId(schedule3.getId())
                    .versionId(version3.getId())
                    .overrideDate(LocalDate.of(2025, 9, 1))
                    .action(Deviation.Action.SKIP)
                    .reason("Labour Day")
                    .build();

            Deviation override6 = Deviation.builder()
                    .scheduleId(schedule3.getId())
                    .versionId(version3.getId())
                    .overrideDate(LocalDate.of(2025, 10, 13))
                    .action(Deviation.Action.FORCE_RUN)
                    .reason("Thanksgiving")
                    .build();

            overrideRepository.saveAll(List.of(override1, override2, override3, override4, override5, override6));
        }
    }
}