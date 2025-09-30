package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RED: Write failing tests for CronExpressionHandler
 * Testing cron-based rule generation using standard cron expressions
 */
class CronExpressionHandlerTest {

    private CronExpressionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CronExpressionHandler();
    }

    @Test
    void shouldGenerateWeekdaysFromCronExpression() {
        // given
        Rule cronRule = Rule.builder()
                .ruleType(Rule.RuleType.CRON_EXPRESSION)
                .ruleConfig("0 0 9 * * MON-FRI")
                .build();

        LocalDate fromDate = LocalDate.of(2025, 1, 1);  // Wednesday
        LocalDate toDate = LocalDate.of(2025, 1, 7);    // Tuesday

        // when
        List<LocalDate> result = handler.generateDates(cronRule, fromDate, toDate);

        // then
        assertEquals(5, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 1))); // Wed
        assertTrue(result.contains(LocalDate.of(2025, 1, 2))); // Thu
        assertTrue(result.contains(LocalDate.of(2025, 1, 3))); // Fri
        assertFalse(result.contains(LocalDate.of(2025, 1, 4))); // Sat - excluded by cron
        assertFalse(result.contains(LocalDate.of(2025, 1, 5))); // Sun - excluded by cron
        assertTrue(result.contains(LocalDate.of(2025, 1, 6))); // Mon
        assertTrue(result.contains(LocalDate.of(2025, 1, 7))); // Tue
    }

    @Test
    void shouldGenerateSpecificDaysFromCronExpression() {
        // given
        Rule cronRule = Rule.builder()
                .ruleType(Rule.RuleType.CRON_EXPRESSION)
                .ruleConfig("0 0 9 * * TUE,FRI")
                .build();

        LocalDate fromDate = LocalDate.of(2025, 1, 1);  // Wednesday
        LocalDate toDate = LocalDate.of(2025, 1, 7);    // Tuesday

        // when
        List<LocalDate> result = handler.generateDates(cronRule, fromDate, toDate);

        // then
        assertEquals(2, result.size());
        assertFalse(result.contains(LocalDate.of(2025, 1, 1))); // Wed - excluded
        assertTrue(result.contains(LocalDate.of(2025, 1, 3)));  // Fri - included
        assertTrue(result.contains(LocalDate.of(2025, 1, 7)));  // Tue - included
    }

    @Test
    void shouldHandleInvalidCronExpression() {
        // given
        Rule invalidCronRule = Rule.builder()
                .ruleType(Rule.RuleType.CRON_EXPRESSION)
                .ruleConfig("invalid-cron")
                .build();

        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 7);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            handler.generateDates(invalidCronRule, fromDate, toDate);
        });
    }

    @Test
    void shouldHandleEmptyDateRangeForValidCron() {
        // given
        Rule cronRule = Rule.builder()
                .ruleType(Rule.RuleType.CRON_EXPRESSION)
                .ruleConfig("0 0 9 * * SUN")  // Only Sundays
                .build();

        LocalDate monday = LocalDate.of(2025, 1, 6);    // Monday
        LocalDate friday = LocalDate.of(2025, 1, 10);   // Friday (no Sundays in range)

        // when
        List<LocalDate> result = handler.generateDates(cronRule, monday, friday);

        // then
        assertTrue(result.isEmpty());
    }
}
