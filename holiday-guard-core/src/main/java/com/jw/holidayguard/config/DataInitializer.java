package com.jw.holidayguard.config;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleOverride;
import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.repository.ScheduleOverrideRepository;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleRuleRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
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
    private final ScheduleOverrideRepository overrideRepository;
    private final ScheduleVersionRepository versionRepository;
    private final ScheduleRuleRepository ruleRepository;

    public DataInitializer(ScheduleRepository scheduleRepository, ScheduleOverrideRepository overrideRepository, ScheduleVersionRepository versionRepository, ScheduleRuleRepository ruleRepository) {
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
            ScheduleVersion version1 = versionRepository.save(ScheduleVersion.builder().scheduleId(schedule1.getId()).active(true).build());
            ruleRepository.save(ScheduleRule.builder().scheduleId(schedule1.getId()).versionId(version1.getId()).ruleType(ScheduleRule.RuleType.US_FEDERAL_RESERVE_BUSINESS_DAYS).build());

            Schedule schedule2 = Schedule.builder()
                    .name("UK Bank Holidays")
                    .description("Official bank holidays in the United Kingdom")
                    .country("UK")
                    .active(true)
                    .build();
            scheduleRepository.save(schedule2);
            ScheduleVersion version2 = versionRepository.save(ScheduleVersion.builder().scheduleId(schedule2.getId()).active(true).build());
            ruleRepository.save(ScheduleRule.builder().scheduleId(schedule2.getId()).versionId(version2.getId()).ruleType(ScheduleRule.RuleType.WEEKDAYS_ONLY).build());

            Schedule schedule3 = Schedule.builder()
                    .name("Canadian Public Holidays")
                    .description("Statutory holidays for Canada")
                    .country("CA")
                    .active(false)
                    .build();
            scheduleRepository.save(schedule3);
            ScheduleVersion version3 = versionRepository.save(ScheduleVersion.builder().scheduleId(schedule3.getId()).active(true).build());
            ruleRepository.save(ScheduleRule.builder().scheduleId(schedule3.getId()).versionId(version3.getId()).ruleType(ScheduleRule.RuleType.WEEKDAYS_ONLY).build());

            Schedule schedule4 = Schedule.builder()
                    .name("Australian Public Holidays")
                    .description("Public holidays across Australia")
                    .country("AU")
                    .active(true)
                    .build();
            scheduleRepository.save(schedule4);
            ScheduleVersion version4 = versionRepository.save(ScheduleVersion.builder().scheduleId(schedule4.getId()).active(true).build());
            ruleRepository.save(ScheduleRule.builder().scheduleId(schedule4.getId()).versionId(version4.getId()).ruleType(ScheduleRule.RuleType.WEEKDAYS_ONLY).build());

            ScheduleOverride override1 = ScheduleOverride.builder()
                    .scheduleId(schedule1.getId())
                    .versionId(version1.getId())
                    .overrideDate(LocalDate.of(2025, 9, 15))
                    .action(ScheduleOverride.OverrideAction.SKIP)
                    .reason("Team Offsite")
                    .build();

            ScheduleOverride override2 = ScheduleOverride.builder()
                    .scheduleId(schedule1.getId())
                    .versionId(version1.getId())
                    .overrideDate(LocalDate.of(2025, 10, 31))
                    .action(ScheduleOverride.OverrideAction.FORCE_RUN)
                    .reason("Halloween Payroll Run")
                    .build();

            ScheduleOverride override3 = ScheduleOverride.builder()
                    .scheduleId(schedule2.getId())
                    .versionId(version2.getId())
                    .overrideDate(LocalDate.of(2025, 9, 22))
                    .action(ScheduleOverride.OverrideAction.SKIP)
                    .reason("UK Team Event")
                    .build();

            ScheduleOverride override4 = ScheduleOverride.builder()
                    .scheduleId(schedule2.getId())
                    .versionId(version2.getId())
                    .overrideDate(LocalDate.of(2025, 10, 27))
                    .action(ScheduleOverride.OverrideAction.FORCE_RUN)
                    .reason("End of Month Processing")
                    .build();

            ScheduleOverride override5 = ScheduleOverride.builder()
                    .scheduleId(schedule3.getId())
                    .versionId(version3.getId())
                    .overrideDate(LocalDate.of(2025, 9, 1))
                    .action(ScheduleOverride.OverrideAction.SKIP)
                    .reason("Labour Day")
                    .build();

            ScheduleOverride override6 = ScheduleOverride.builder()
                    .scheduleId(schedule3.getId())
                    .versionId(version3.getId())
                    .overrideDate(LocalDate.of(2025, 10, 13))
                    .action(ScheduleOverride.OverrideAction.FORCE_RUN)
                    .reason("Thanksgiving")
                    .build();

            ScheduleOverride override7 = ScheduleOverride.builder()
                    .scheduleId(schedule4.getId())
                    .versionId(version4.getId())
                    .overrideDate(LocalDate.of(2025, 9, 26))
                    .action(ScheduleOverride.OverrideAction.SKIP)
                    .reason("Friday before AFL Grand Final")
                    .build();

            ScheduleOverride override8 = ScheduleOverride.builder()
                    .scheduleId(schedule4.getId())
                    .versionId(version4.getId())
                    .overrideDate(LocalDate.of(2025, 10, 6))
                    .action(ScheduleOverride.OverrideAction.FORCE_RUN)
                    .reason("Labour Day")
                    .build();

            overrideRepository.saveAll(List.of(override1, override2, override3, override4, override5, override6, override7, override8));
        }
    }
}