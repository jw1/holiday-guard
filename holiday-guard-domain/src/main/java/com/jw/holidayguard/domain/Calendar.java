package com.jw.holidayguard.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Calendar is an aggregate root that encapsulates a schedule with its rule and deviations.
 * It provides a unified interface to answer "should run" queries without requiring database access.
 *
 * <p>This object model improvement addresses several goals:
 * <ul>
 *   <li>Single object can answer shouldRun() for any date</li>
 *   <li>Both single-date and date-range queries use the exact same algorithm</li>
 *   <li>Business logic is decoupled from database/repositories</li>
 *   <li>Testable without mocks (pure domain logic)</li>
 * </ul>
 *
 * <p>Design notes:
 * <ul>
 *   <li>This is a POJO, not a JPA @Entity (composition, not persistence)</li>
 *   <li>Deviations always take precedence over rules</li>
 *   <li>Single-date query is optimized (O(1) for deviation lookup + O(1) for rule evaluation)</li>
 *   <li>Date-range query internally calls single-date query for consistency</li>
 * </ul>
 */
@Getter
public class Calendar {

    private final Schedule schedule;
    private final Rule rule;
    private final List<Deviation> deviations;

    @JsonIgnore  // Don't serialize the evaluator - it's a strategy, not data
    private final RuleEvaluator ruleEvaluator;

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Creates a Calendar with the given schedule, rule, and deviations.
     *
     * @param schedule The schedule this calendar represents
     * @param rule The rule that determines when the schedule should run
     * @param deviations List of deviations that override the rule for specific dates
     * @param ruleEvaluator Strategy for evaluating whether a rule matches a date
     */
    public Calendar(Schedule schedule, Rule rule, List<Deviation> deviations, RuleEvaluator ruleEvaluator) {
        this.schedule = schedule;
        this.rule = rule;
        this.deviations = deviations != null ? deviations : List.of();
        this.ruleEvaluator = ruleEvaluator;
    }

    /**
     * Checks if the schedule should run on a specific date.
     * This is the core algorithm used by both single-date and date-range queries.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Check for deviations first (they take precedence)</li>
     *   <li>If deviation exists: return FORCE_RUN → true, FORCE_SKIP → false</li>
     *   <li>If no deviation: evaluate rule using RuleEvaluator</li>
     * </ol>
     *
     * @param date The date to check
     * @return true if schedule should run on this date, false otherwise
     */
    public boolean shouldRun(LocalDate date) {

        var deviation = findDeviationForDate(date);

        // use deviation or fallback to rule
        return deviation
                .map(Deviation::shouldRun)
                .orElseGet(() -> ruleEvaluator.shouldRun(rule, date));

        // No deviation found, evaluate rule
    }

    /**
     * Checks if the schedule should run for each date in the given range (inclusive).
     * This method ensures consistency with single-date queries by calling shouldRun(date)
     * for each date in the range.
     *
     * <p>If start is after end, returns an empty map.
     *
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return Map of date → boolean indicating whether schedule should run on each date
     */
    public Map<LocalDate, Boolean> shouldRun(LocalDate start, LocalDate end) {

        Map<LocalDate, Boolean> results = new LinkedHashMap<>();
        if (start.isAfter(end)) return results;

        // iterate through date range, using same algorithm as single-date query
        LocalDate current = start;

        while (! current.isAfter(end)) {
            results.put(current, shouldRun(current)); // Delegates to single-date method
            current = current.plusDays(1);
        }

        return results;
    }

    /**
     * Finds a deviation for the given date, if one exists.
     *
     * @param date The date to look up
     * @return Optional containing the deviation, or empty if none exists
     */
    private Optional<Deviation> findDeviationForDate(LocalDate date) {
        return deviations.stream()
                .filter(d -> d.getDeviationDate().equals(date))
                .findFirst();
    }

    /**
     * serialize to json
     * <p>
     * RuleEvaluator strategy is not serialized (it's behavior, not data).
     * Use fromJson() with a RuleEvaluator to reconstruct
     *
     * @return JSON string representation of this calendar
     * @throws RuntimeException if serialization fails
     */
    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Calendar to JSON", e);
        }
    }

    /**
     * deserialize from json
     * Must provide RuleEvaluator as it's not serialized
     *
     * @param json JSON string representation of a calendar
     * @param ruleEvaluator The rule evaluator strategy to use
     * @return Reconstructed Calendar instance
     * @throws RuntimeException if deserialization fails
     */
    public static Calendar fromJson(String json, RuleEvaluator ruleEvaluator) {
        try {
            var data = mapper.readValue(json, CalendarData.class);

            return new Calendar(
                    data.schedule(),
                    data.rule(),
                    data.deviations(),
                    ruleEvaluator);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize Calendar from JSON", e);
        }
    }

    /**
     * Internal DTO for JSON deserialization.
     * Needed because Calendar has a @JsonIgnore field (ruleEvaluator) that we don't want to deserialize.
     */
    private record CalendarData(Schedule schedule, Rule rule, List<Deviation> deviations) {
    }

    /**
     * Strategy interface for evaluating rules.
     * <p>
     * Allows Calendar to work with any rule evaluation logic
     * without depending on specific implementations (RuleEngine, handlers, etc.).
     */
    public interface RuleEvaluator {

        /**
         * Evaluates whether a rule indicates the schedule should run on a given date.
         *
         * @param rule The rule to evaluate
         * @param date The date to check
         * @return true if rule indicates schedule should run, false otherwise
         */
        boolean shouldRun(Rule rule, LocalDate date);
    }
}
