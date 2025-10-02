package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.Rule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
public class NoDaysHandler implements RuleHandler {

    @Override
    public Rule.RuleType getSupportedRuleType() {
        return Rule.RuleType.NO_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate fromDate, LocalDate toDate) {
        return Collections.emptyList();
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        return false;
    }
}
