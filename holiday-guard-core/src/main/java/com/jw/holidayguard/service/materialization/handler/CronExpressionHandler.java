package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRule;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GREEN: Handler for CRON_EXPRESSION rule type.
 * Generates dates based on standard cron expressions.
 */
@Component
public class CronExpressionHandler implements RuleHandler {

    @Override
    public List<LocalDate> generateDates(ScheduleRule rule, LocalDate fromDate, LocalDate toDate) {
        // Handle invalid date range
        if (fromDate.isAfter(toDate)) {
            return Collections.emptyList();
        }

        String cronConfig = rule.getRuleConfig();
        if (cronConfig == null || cronConfig.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron expression cannot be null or empty");
        }

        try {
            CronExpression cronExpression = CronExpression.parse(cronConfig);
            return generateDatesFromCron(cronExpression, fromDate, toDate);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronConfig, e);
        }
    }

    private List<LocalDate> generateDatesFromCron(CronExpression cronExpression, LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> dates = new ArrayList<>();

        // Start checking from the beginning of the fromDate
        LocalDateTime current = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

        // Find the next execution time starting from current
        while (current.toLocalDate().isBefore(toDate) || current.toLocalDate().equals(toDate)) {
            LocalDateTime nextExecution = cronExpression.next(current);

            if (nextExecution == null || nextExecution.isAfter(endDateTime)) {
                break;
            }

            LocalDate executionDate = nextExecution.toLocalDate();

            // Only add dates within our range and avoid duplicates
            if (!executionDate.isBefore(fromDate) && !executionDate.isAfter(toDate) && !dates.contains(executionDate)) {
                dates.add(executionDate);
            }

            // Move to the next day to find the next execution
            current = nextExecution.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        return dates;
    }

    @Override
    public ScheduleRule.RuleType getSupportedRuleType() {
        return ScheduleRule.RuleType.CRON_EXPRESSION;
    }
}
