package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.service.materialization.handler.RuleHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * REFACTOR: Improved implementation using handler pattern
 * Implementation of RuleEngine that delegates to specific rule handlers
 * and generates materialized calendar dates.
 */
@Service
public class RuleEngineImpl implements RuleEngine {

    private final Map<ScheduleRule.RuleType, RuleHandler> handlers;

    public RuleEngineImpl(List<RuleHandler> ruleHandlers) {
        this.handlers = ruleHandlers.stream()
                .collect(Collectors.toMap(
                        RuleHandler::getSupportedRuleType,
                        Function.identity()
                ));
    }

    @Override
    public List<LocalDate> generateDates(ScheduleRule rule, LocalDate fromDate, LocalDate toDate) {

        // empty list if not valid range
        if (fromDate.isAfter(toDate)) return List.of();

        RuleHandler handler = handlers.get(rule.getRuleType());
        if (handler == null) {
            throw new UnsupportedOperationException("No handler found for rule type: " + rule.getRuleType());
        }

        return handler.generateDates(rule, fromDate, toDate);
    }

    @Override
    public boolean shouldRun(ScheduleRule rule, LocalDate date) {
        RuleHandler handler = handlers.get(rule.getRuleType());
        if (handler == null) {
            throw new UnsupportedOperationException("No handler found for rule type: " + rule.getRuleType());
        }

        return handler.shouldRun(rule, date);
    }
}
