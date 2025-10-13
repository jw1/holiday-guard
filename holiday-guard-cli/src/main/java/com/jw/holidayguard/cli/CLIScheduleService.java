package com.jw.holidayguard.cli;

import com.jw.holidayguard.domain.Calendar;
import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.RunStatus;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.service.rule.RuleEngine;
import com.jw.holidayguard.service.rule.RuleEngineImpl;
import com.jw.holidayguard.service.rule.handler.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for building Calendar objects from CLI configuration and evaluating shouldRun logic.
 */
public class CLIScheduleService {

    private final RuleEngine ruleEngine;

    public CLIScheduleService() {
        // Manually instantiate all rule handlers (no Spring DI in CLI)
        List<RuleHandler> handlers = List.of(
            new WeekdaysOnlyHandler(),
            new CronExpressionHandler(),
            new USFederalReserveBusinessDaysHandler(),
            new AllDaysHandler(),
            new NoDaysHandler()
        );
        this.ruleEngine = new RuleEngineImpl(handlers);
    }

    /**
     * Build a Calendar object from CLI configuration.
     *
     * @param scheduleConfig the schedule configuration
     * @return Calendar ready for shouldRun queries
     */
    public Calendar buildCalendar(CLIConfig.ScheduleConfig scheduleConfig) {
        // Create Schedule domain object
        Schedule schedule = Schedule.builder()
            .name(scheduleConfig.getName())
            .description(scheduleConfig.getDescription())
            .active(true)
            .build();

        // Create Rule domain object
        Rule rule = Rule.builder()
            .ruleType(Rule.RuleType.valueOf(scheduleConfig.getRule().getRuleType()))
            .ruleConfig(scheduleConfig.getRule().getRuleConfig())
            .build();

        // Create Deviation domain objects
        List<Deviation> deviations = new ArrayList<>();
        if (scheduleConfig.getDeviations() != null) {
            deviations = scheduleConfig.getDeviations().stream()
                .map(dev -> Deviation.builder()
                    .deviationDate(dev.getDate())
                    .action(RunStatus.valueOf(dev.getAction()))
                    .reason(dev.getReason())
                    .build())
                .collect(Collectors.toList());
        }

        // Create Calendar using domain factory method
        return new Calendar(schedule, rule, deviations, ruleEngine::shouldRun);
    }

    /**
     * Determine the detailed RunStatus for a date.
     *
     * @param calendar the calendar to query
     * @param scheduleConfig the schedule configuration
     * @param date the date to check
     * @return detailed RunStatus (RUN, SKIP, FORCE_RUN, FORCE_SKIP)
     */
    public RunStatus determineRunStatus(Calendar calendar, CLIConfig.ScheduleConfig scheduleConfig, LocalDate date) {
        boolean shouldRun = calendar.shouldRun(date);

        // Check if there's a deviation for this date
        if (scheduleConfig.getDeviations() != null) {
            for (CLIConfig.DeviationConfig deviation : scheduleConfig.getDeviations()) {
                if (deviation.getDate().equals(date)) {
                    return RunStatus.valueOf(deviation.getAction());
                }
            }
        }

        // No deviation, return based on rule
        return shouldRun ? RunStatus.RUN : RunStatus.SKIP;
    }
}
