package com.jw.holidayguard.service.rule;

import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.service.rule.handler.RuleHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of RuleEngine that delegates to specific rule handlers
 * and generates materialized calendar dates.
 */
@Service
public class RuleEngineImpl implements RuleEngine {

    private final Map<Rule.RuleType, RuleHandler> handlers;

    public RuleEngineImpl(List<RuleHandler> ruleHandlers) {
        this.handlers = ruleHandlers.stream()
                .collect(Collectors.toMap(
                        RuleHandler::getSupportedRuleType,
                        Function.identity()
                ));
    }

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to) {

        // empty list if not valid range
        if (from.isAfter(to)) return List.of();

        return getRuleHandler(rule).generateDates(rule, from, to);
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        return getRuleHandler(rule).shouldRun(rule, date);
    }

    private RuleHandler getRuleHandler(Rule rule) {
        return Optional
                .ofNullable(handlers.get(rule.getRuleType()))
                .orElseThrow(() -> new UnsupportedOperationException("No handler found for rule type: " + rule.getRuleType()));
    }
}
