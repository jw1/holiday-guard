package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.Rule;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.jw.holidayguard.domain.Rule.RuleType.CRON_EXPRESSION;

/**
 * Handles date generation for rules based on a cron expression.
 * <p>
 * This implementation uses Spring Framework's {@link CronExpression}, which supports a six-field format.
 * The fields are separated by spaces and represent:
 * <ol>
 *     <li>Seconds (0-59)</li>
 *     <li>Minutes (0-59)</li>
 *     <li>Hours (0-23)</li>
 *     <li>Day of Month (1-31)</li>
 *     <li>Month (1-12 or JAN-DEC)</li>
 *     <li>Day of Week (0-7 or SUN-SAT, where both 0 and 7 are Sunday)</li>
 * </ol>
 * <p>
 * Special characters are also supported:
 * <ul>
 *     <li>{@code *} - Matches any value.</li>
 *     <li>{@code ?} - No specific value. Used when specifying a day-of-week and not a day-of-month, or vice-versa.</li>
 *     <li>{@code ,} - Separates multiple values... such as 1,2,3</li>
 *     <li>{@code -} - Defines a range... such as MON-FRI or 4-9</li>
 *     <li>{@code /} - Specifies increments. For example, {@code 1/2} in the day-of-month field means every 2 days starting on the 1st (i.e., odd-numbered days).</li>
 * </ul>
 * <p>
 * Since this application is only concerned with dates, time fields (seconds, minutes, hours) should
 * be set to start of day, for example {@code "0 0 0 ..."}.
 * <p>
 * Example: To run on the 15th of every month, the expression would be {@code "0 0 0 15 * ?"}.
 */
@Service
public class CronExpressionHandler implements RuleHandler {

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to) {
        CronExpression cron = CronExpression.parse(rule.getRuleConfig());

        // accumulate business days in requested date range
        var dates = new ArrayList<LocalDate>();

        LocalDate day = from;

        // step through days one at a time
        // accumulate if current day is same as next execution of cron (i.e. it runs that day)
        while (!day.isAfter(to)) {

            // find next execution of cron (starting from "day")
            LocalDateTime nextExecution = cron.next(day.atStartOfDay());

            // if it's not after "day", then it runs that day
            if (nextExecution != null && ! nextExecution.toLocalDate().isAfter(day)) {
                dates.add(day);
            }

            day = day.plusDays(1);
        }

        return dates;
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        CronExpression cron = CronExpression.parse(rule.getRuleConfig());

        // what's the next execution after yesterday
        LocalDateTime nextExecution = cron.next(date.atStartOfDay().minusDays(1));

        // true if next execution exists, and it equals "date"
        return nextExecution != null && nextExecution.toLocalDate().equals(date);
    }

    @Override
    public Rule.RuleType getSupportedRuleType() {
        return CRON_EXPRESSION;
    }
}
