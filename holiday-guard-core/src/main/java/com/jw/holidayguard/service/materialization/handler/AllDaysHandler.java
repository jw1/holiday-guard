package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllDaysHandler implements RuleHandler {

    @Override
    public ScheduleRule.RuleType getSupportedRuleType() {
        return ScheduleRule.RuleType.ALL_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(ScheduleRule rule, LocalDate fromDate, LocalDate toDate) {
        return fromDate.datesUntil(toDate.plusDays(1)).collect(Collectors.toList());
    }
}