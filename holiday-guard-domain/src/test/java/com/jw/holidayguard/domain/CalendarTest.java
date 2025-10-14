package com.jw.holidayguard.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test-driven development for Calendar domain object.
 * Calendar encapsulates a schedule with its rule and deviations,
 * providing a unified interface to answer "should run" queries.
 */
class CalendarTest {

    private Schedule schedule;
    private Rule weekdaysRule;
    private List<Deviation> emptyDeviations;
    private MockRuleEvaluator mockRuleEvaluator;

    @BeforeEach
    void setUp() {
        schedule = Schedule.builder()
                .id(1L)
                .name("Payroll Schedule")
                .description("Weekday payroll processing")
                .build();

        weekdaysRule = Rule.builder()
                .id(10L)
                .scheduleId(1L)
                .versionId(100L)
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .build();

        emptyDeviations = List.of();
        mockRuleEvaluator = new MockRuleEvaluator();
    }

    @Test
    void shouldRunOnSingleDate_whenRuleMatches() {

        // given - calendar with weekdays-only rule
        Calendar calendar = new Calendar(schedule, weekdaysRule, emptyDeviations, mockRuleEvaluator);
        LocalDate monday = LocalDate.of(2025, 1, 6); // known "Monday"
        mockRuleEvaluator.setResult(true);

        // when - Checking if should run on Monday
        boolean result = calendar.shouldRun(monday);

        // then - Should return true
        assertTrue(result);
        assertThat(mockRuleEvaluator.getLastQueriedDate()).isEqualTo(monday);
    }

    @Test
    void shouldNotRunOnSingleDate_whenRuleDoesNotMatch() {
        // given - A calendar with weekdays-only rule
        Calendar calendar = new Calendar(schedule, weekdaysRule, emptyDeviations, mockRuleEvaluator);
        LocalDate saturday = LocalDate.of(2025, 1, 4); // Saturday
        mockRuleEvaluator.setResult(false);

        // when - Checking if should run on Saturday
        boolean result = calendar.shouldRun(saturday);

        // then - Should return false
        assertFalse(result);
        assertThat(mockRuleEvaluator.getLastQueriedDate()).isEqualTo(saturday);
    }

    @Test
    void shouldApplySkipDeviation_overridingRule() {
        // given - A calendar with a SKIP deviation on Monday
        LocalDate monday = LocalDate.of(2025, 1, 6);
        Deviation skipDeviation = Deviation.builder()
                .deviationDate(monday)
                .action(RunStatus.FORCE_SKIP)
                .reason("Holiday")
                .build();

        Calendar calendar = new Calendar(schedule, weekdaysRule, List.of(skipDeviation), mockRuleEvaluator);
        mockRuleEvaluator.setResult(true); // Rule says run

        // when - Checking if should run on Monday (with skip deviation)
        boolean result = calendar.shouldRun(monday);

        // then - Deviation overrides rule, should NOT run
        assertFalse(result);
    }

    @Test
    void shouldApplyForceRunDeviation_overridingRule() {
        // given - A calendar with a FORCE_RUN deviation on Saturday
        LocalDate saturday = LocalDate.of(2025, 1, 4);
        Deviation forceRunDeviation = Deviation.builder()
                .scheduleId(1L)
                .versionId(100L)
                .deviationDate(saturday)
                .action(RunStatus.FORCE_RUN)
                .reason("Special processing day")
                .build();

        Calendar calendar = new Calendar(schedule, weekdaysRule, List.of(forceRunDeviation), mockRuleEvaluator);
        mockRuleEvaluator.setResult(false); // Rule says don't run

        // when - Checking if should run on Saturday (with force run deviation)
        boolean result = calendar.shouldRun(saturday);

        // then - Deviation overrides rule, SHOULD run
        assertTrue(result);
    }

    @Test
    void shouldRunOnDateRange_returnsCorrectMap() {
        // given - A calendar and a date range
        Calendar calendar = new Calendar(schedule, weekdaysRule, emptyDeviations, mockRuleEvaluator);
        LocalDate start = LocalDate.of(2025, 1, 6); // Monday
        LocalDate end = LocalDate.of(2025, 1, 10);   // Friday

        // Mock engine returns true for weekdays
        mockRuleEvaluator.setResultByDay(day -> {
            int dayOfWeek = day.getDayOfWeek().getValue();
            return dayOfWeek >= 1 && dayOfWeek <= 5; // Mon-Fri
        });

        // when - Checking should run for date range
        Map<LocalDate, Boolean> results = calendar.shouldRun(start, end);

        // then - Should return correct results for each day
        assertThat(results).hasSize(5);
        assertTrue(results.get(LocalDate.of(2025, 1, 6)));  // Mon
        assertTrue(results.get(LocalDate.of(2025, 1, 7)));  // Tue
        assertTrue(results.get(LocalDate.of(2025, 1, 8)));  // Wed
        assertTrue(results.get(LocalDate.of(2025, 1, 9)));  // Thu
        assertTrue(results.get(LocalDate.of(2025, 1, 10))); // Fri
    }

    @Test
    void dateRangeShouldRespectDeviations() {
        // given - A calendar with deviation on Wednesday
        LocalDate wednesday = LocalDate.of(2025, 1, 8);
        Deviation skipWednesday = Deviation.builder()
                .scheduleId(1L)
                .versionId(100L)
                .deviationDate(wednesday)
                .action(RunStatus.FORCE_SKIP)
                .reason("Mid-week holiday")
                .build();

        Calendar calendar = new Calendar(schedule, weekdaysRule, List.of(skipWednesday), mockRuleEvaluator);
        LocalDate start = LocalDate.of(2025, 1, 6); // Monday
        LocalDate end = LocalDate.of(2025, 1, 10);   // Friday

        mockRuleEvaluator.setResultByDay(day -> {
            int dayOfWeek = day.getDayOfWeek().getValue();
            return dayOfWeek >= 1 && dayOfWeek <= 5; // All weekdays
        });

        // when - Checking range
        Map<LocalDate, Boolean> results = calendar.shouldRun(start, end);

        // then - Wednesday should be false due to deviation
        assertTrue(results.get(LocalDate.of(2025, 1, 6)));  // Mon - true
        assertTrue(results.get(LocalDate.of(2025, 1, 7)));  // Tue - true
        assertFalse(results.get(LocalDate.of(2025, 1, 8))); // Wed - false (deviation)
        assertTrue(results.get(LocalDate.of(2025, 1, 9)));  // Thu - true
        assertTrue(results.get(LocalDate.of(2025, 1, 10))); // Fri - true
    }

    @Test
    void bothMethodsUseConsistentAlgorithm() {
        // given - Same calendar and date
        Calendar calendar = new Calendar(schedule, weekdaysRule, emptyDeviations, mockRuleEvaluator);
        LocalDate testDate = LocalDate.of(2025, 1, 6); // Monday
        mockRuleEvaluator.setResult(true);

        // when - Querying single date and also querying as range
        boolean singleResult = calendar.shouldRun(testDate);
        Map<LocalDate, Boolean> rangeResult = calendar.shouldRun(testDate, testDate);

        // then - Both should return same result
        assertThat(singleResult).isEqualTo(rangeResult.get(testDate));

        // Try with multiple dates to ensure consistency
        LocalDate start = LocalDate.of(2025, 1, 6);
        LocalDate end = LocalDate.of(2025, 1, 8);
        mockRuleEvaluator.setResultByDay(day -> day.getDayOfWeek().getValue() <= 5);

        Map<LocalDate, Boolean> rangeResults = calendar.shouldRun(start, end);

        // Each individual query should match the range query
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate currentDate = date; // for lambda
            mockRuleEvaluator.setResultByDay(d -> d.getDayOfWeek().getValue() <= 5);
            boolean individualResult = calendar.shouldRun(currentDate);
            assertThat(individualResult)
                    .withFailMessage("Date %s: individual query != range query", currentDate)
                    .isEqualTo(rangeResults.get(currentDate));
        }
    }

    @Test
    void emptyDateRange_returnsEmptyMap() {
        // given - Calendar and invalid date range (end before start)
        Calendar calendar = new Calendar(schedule, weekdaysRule, emptyDeviations, mockRuleEvaluator);
        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 5);

        // when - Querying invalid range
        Map<LocalDate, Boolean> results = calendar.shouldRun(start, end);

        // then - Should return empty map
        assertThat(results).isEmpty();
    }

    @Test
    void toJsonSerializesCompleteState() throws Exception {
        // given - A calendar with complete state (schedule, rule, deviations)
        LocalDate monday = LocalDate.of(2025, 1, 6);
        Deviation skipDeviation = Deviation.builder()
                .id(5L)
                .scheduleId(1L)
                .versionId(100L)
                .deviationDate(monday)
                .action(RunStatus.FORCE_SKIP)
                .reason("Holiday")
                .build();

        Calendar calendar = new Calendar(schedule, weekdaysRule, List.of(skipDeviation), mockRuleEvaluator);

        // when - Converting to JSON
        String json = calendar.toJson();

        // then - JSON should contain all essential fields
        assertThat(json).isNotNull();
        assertThat(json).contains("\"name\":\"Payroll Schedule\"");
        assertThat(json).contains("\"ruleType\":\"WEEKDAYS_ONLY\"");
        assertThat(json).contains("\"action\":\"FORCE_SKIP\"");
        assertThat(json).contains("\"reason\":\"Holiday\"");
    }

    @Test
    void fromJsonReconstructsCalendar() throws Exception {
        // given - A calendar and its JSON representation
        LocalDate monday = LocalDate.of(2025, 1, 6);
        Deviation skipDeviation = Deviation.builder()
                .id(5L)
                .scheduleId(1L)
                .versionId(100L)
                .deviationDate(monday)
                .action(RunStatus.FORCE_SKIP)
                .reason("Holiday")
                .build();

        Calendar originalCalendar = new Calendar(schedule, weekdaysRule, List.of(skipDeviation), mockRuleEvaluator);
        String json = originalCalendar.toJson();

        // when - Reconstructing calendar from JSON
        Calendar reconstructedCalendar = Calendar.fromJson(json, mockRuleEvaluator);

        // then - Reconstructed calendar should have same state
        assertThat(reconstructedCalendar.getSchedule().getName()).isEqualTo(schedule.getName());
        assertThat(reconstructedCalendar.getSchedule().getDescription()).isEqualTo(schedule.getDescription());
        assertThat(reconstructedCalendar.getRule().getRuleType()).isEqualTo(weekdaysRule.getRuleType());
        assertThat(reconstructedCalendar.getDeviations()).hasSize(1);
        assertThat(reconstructedCalendar.getDeviations().get(0).getAction()).isEqualTo(RunStatus.FORCE_SKIP);
        assertThat(reconstructedCalendar.getDeviations().get(0).getReason()).isEqualTo("Holiday");
    }

    @Test
    void roundTripJsonPreservesCalendarBehavior() throws Exception {
        // given - A calendar with specific behavior
        LocalDate wednesday = LocalDate.of(2025, 1, 8);
        Deviation skipWednesday = Deviation.builder()
                .scheduleId(1L)
                .versionId(100L)
                .deviationDate(wednesday)
                .action(RunStatus.FORCE_SKIP)
                .reason("Mid-week holiday")
                .build();

        Calendar originalCalendar = new Calendar(schedule, weekdaysRule, List.of(skipWednesday), mockRuleEvaluator);

        // Set up mock to return consistent results
        mockRuleEvaluator.setResultByDay(day -> day.getDayOfWeek().getValue() <= 5);

        // when - Round-tripping through JSON
        String json = originalCalendar.toJson();
        Calendar reconstructedCalendar = Calendar.fromJson(json, mockRuleEvaluator);

        // then - Reconstructed calendar should behave identically
        LocalDate monday = LocalDate.of(2025, 1, 6);
        LocalDate tuesday = LocalDate.of(2025, 1, 7);

        // Both should have same deviation behavior
        assertFalse(reconstructedCalendar.shouldRun(wednesday), "Wednesday should be skipped (deviation)");
        assertTrue(reconstructedCalendar.shouldRun(monday), "Monday should run");
        assertTrue(reconstructedCalendar.shouldRun(tuesday), "Tuesday should run");
    }

    /**
     * Mock RuleEvaluator for testing Calendar logic in isolation.
     * Will return "result" unless a function is specified.
     * <p>
     * Sample usage: Weekdays return true, weekends return false
     * evaluator.setResultByDay(date -> date.getDayOfWeek().getValue() <= 5);
     * boolean monday = calendar.shouldRun(LocalDate.of(2025, 1, 6));    // true
     * boolean saturday = calendar.shouldRun(LocalDate.of(2025, 1, 4));  // false
     */
    private static class MockRuleEvaluator implements Calendar.RuleEvaluator {

        private boolean result;
        private Function<LocalDate, Boolean> resultFunction;

        // Observability: track the last date that was evaluated (for test assertions)
        @Getter
        private LocalDate lastQueriedDate;

        /**
         * Sets a fixed result to return for all dates (simple mode).
         * Clears any previously set function.
         *
         * @param result The boolean to return for all shouldRun calls
         */
        public void setResult(boolean result) {
            this.result = result;
            this.resultFunction = null;  // Clear function mode
        }

        /**
         * Allows caller to provide function that determines whether true or
         * false is returned.
         *
         * @param function Function that takes LocalDate and returns boolean
         */
        public void setResultByDay(java.util.function.Function<LocalDate, Boolean> function) {
            this.resultFunction = function;
        }

        /**
         * Check resultFunction for answer.  If not set, just return result.
         *
         * @param rule Ignored by this mock class
         * @param date The date to evaluate for
         * @return configured boolean result
         */
        @Override
        public boolean shouldRun(Rule rule, LocalDate date) {
            // Always record what date was queried (enables test assertions)
            this.lastQueriedDate = date;

            // Function mode takes precedence over simple mode
            if (resultFunction != null) return resultFunction.apply(date);

            // Fall back to simple mode
            return result;
        }

    }
}
