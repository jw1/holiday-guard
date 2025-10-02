package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.Rule;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.jw.holidayguard.domain.Rule.RuleType.WEEKDAYS_ONLY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

/**
 * Generates Monday through Friday dates, excluding weekends.
 */
@Component
public class WeekdaysOnlyHandler implements RuleHandler {

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to) {

        // invalid date range -> return empty list
        if (from.isAfter(to)) return List.of();

        // accumulate weekdays in requested date range
        var weekdays = new ArrayList<LocalDate>();

        LocalDate day = from;

        while (! day.isAfter(to)) {
            DayOfWeek dayOfWeek = day.getDayOfWeek();

            if (dayOfWeek != SATURDAY && dayOfWeek != SUNDAY) {
                weekdays.add(day);
            }

            day = day.plusDays(1);
        }

        return weekdays;
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != SATURDAY && day != SUNDAY;
    }

    @Override
    public Rule.RuleType getSupportedRuleType() {
        return WEEKDAYS_ONLY;
    }
}
