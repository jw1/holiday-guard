package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.Rule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.jw.holidayguard.domain.Rule.RuleType.NO_DAYS;

/**
 * Simply returns false for all days
 * <p>
 * Use when your schedule rarely runs.  Deviations can be used for
 * all run days.
 */
@Component
public class NoDaysHandler implements RuleHandler {

    @Override
    public Rule.RuleType getSupportedRuleType() {
        return NO_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1)).toList();
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        return false;
    }
}
