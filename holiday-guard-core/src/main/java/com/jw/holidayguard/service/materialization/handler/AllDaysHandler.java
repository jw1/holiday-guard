package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.Rule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllDaysHandler implements RuleHandler {

    @Override
    public Rule.RuleType getSupportedRuleType() {
        return Rule.RuleType.ALL_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate fromDate, LocalDate toDate) {
        return fromDate.datesUntil(toDate.plusDays(1)).collect(Collectors.toList());
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        return true;
    }
}