package com.jw.holidayguard.service.rule.handler;

import com.jw.holidayguard.domain.Rule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.jw.holidayguard.domain.Rule.RuleType.ALL_DAYS;

/**
 * Simply returns true for all days
 * <p>
 * Use when your schedule needs almost every day, and deviations can plug the
 * gaps.
 */
@Component
public class AllDaysHandler implements RuleHandler {

    @Override
    public Rule.RuleType getSupportedRuleType() {
        return ALL_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1)).toList();
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        return true;
    }
}