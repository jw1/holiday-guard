package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRules;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllDaysHandler implements RuleHandler {

    @Override
    public ScheduleRules.RuleType getSupportedRuleType() {
        return ScheduleRules.RuleType.ALL_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(ScheduleRules rule, LocalDate fromDate, LocalDate toDate) {
        return fromDate.datesUntil(toDate.plusDays(1)).collect(Collectors.toList());
    }
}
