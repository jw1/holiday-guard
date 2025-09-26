package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.domain.ScheduleRule.RuleType;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CronExpressionHandler implements RuleHandler {

    @Override
    public List<LocalDate> generateDates(ScheduleRule rule, LocalDate fromDate, LocalDate toDate) {
        CronExpression cron = CronExpression.parse(rule.getRuleConfig());
        List<LocalDate> dates = new ArrayList<>();

        LocalDate currentDate = fromDate;
        while (!currentDate.isAfter(toDate)) {
            LocalDateTime nextExecution = cron.next(currentDate.atStartOfDay());
            if (nextExecution != null && !nextExecution.toLocalDate().isAfter(currentDate)) {
                dates.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        return dates;
    }

    @Override
    public boolean shouldRun(ScheduleRule rule, LocalDate date) {
        CronExpression cron = CronExpression.parse(rule.getRuleConfig());
        LocalDateTime nextExecution = cron.next(date.atStartOfDay().minusDays(1));
        return nextExecution != null && nextExecution.toLocalDate().equals(date);
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
