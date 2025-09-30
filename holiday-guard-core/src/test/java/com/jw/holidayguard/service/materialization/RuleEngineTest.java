package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.service.materialization.handler.WeekdaysOnlyHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * REFACTOR: Updated tests to work with handler pattern
 * Tests for RuleEngine interface using dependency injection
 */
class RuleEngineTest {

    private RuleEngine engine;

    @BeforeEach
    void setUp() {
        // Create engine with WeekdaysOnlyHandler
        engine = new RuleEngineImpl(List.of(new WeekdaysOnlyHandler()));
    }

    @Test
    void shouldGenerateDatesForWeekdaysOnlyRule() {
        Rule weekdaysRule = Rule.builder()
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .ruleConfig(null)
                .build();

        LocalDate fromDate = LocalDate.of(2025, 1, 1);  // Wednesday
        LocalDate toDate = LocalDate.of(2025, 1, 7);    // Tuesday

        List<LocalDate> result = engine.generateDates(weekdaysRule, fromDate, toDate);

        // Should generate: Jan 1 (Wed), Jan 2 (Thu), Jan 3 (Fri), Jan 6 (Mon), Jan 7 (Tue)
        // Should skip: Jan 4 (Sat), Jan 5 (Sun)
        assertEquals(5, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 1))); // Wed
        assertTrue(result.contains(LocalDate.of(2025, 1, 2))); // Thu
        assertTrue(result.contains(LocalDate.of(2025, 1, 3))); // Fri
        assertFalse(result.contains(LocalDate.of(2025, 1, 4))); // Sat - should be excluded
        assertFalse(result.contains(LocalDate.of(2025, 1, 5))); // Sun - should be excluded
        assertTrue(result.contains(LocalDate.of(2025, 1, 6))); // Mon
        assertTrue(result.contains(LocalDate.of(2025, 1, 7))); // Tue
    }

    @Test
    void shouldReturnEmptyListWhenDateRangeIsInvalid() {
        Rule rule = Rule.builder()
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .build();

        LocalDate fromDate = LocalDate.of(2025, 1, 10);
        LocalDate toDate = LocalDate.of(2025, 1, 5);  // toDate before fromDate

        List<LocalDate> result = engine.generateDates(rule, fromDate, toDate);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionForUnsupportedRuleType() {
        Rule unsupportedRule = Rule.builder()
                .ruleType(Rule.RuleType.CRON_EXPRESSION)  // Not supported yet
                .build();

        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 7);

        assertThrows(UnsupportedOperationException.class, () -> {
            engine.generateDates(unsupportedRule, fromDate, toDate);
        });
    }
}
