package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.util.ACHProcessingScheduleFactory.USFederalHolidays;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static com.jw.holidayguard.domain.Rule.RuleType.US_FEDERAL_RESERVE_BUSINESS_DAYS;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

/**
 * Provides business logic used to populate a US Fed Business day calendar
 * (basically weekdays, excluding federal holidays)
 */
@Component
public class USFederalReserveBusinessDaysHandler implements RuleHandler {

    @Override
    public Rule.RuleType getSupportedRuleType() {
        return US_FEDERAL_RESERVE_BUSINESS_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1))
                .filter(this::isBusinessDay)
                .toList();
    }

    @Override
    public boolean shouldRun(Rule rule, LocalDate date) {
        return isBusinessDay(date);
    }

    private boolean isBusinessDay(LocalDate date) {

        // weekends are not business days
        DayOfWeek day = date.getDayOfWeek();
        if (day == SATURDAY || day == SUNDAY) return false;

        // holidays are not business days
        List<LocalDate> holidays = USFederalHolidays.getHolidays(date.getYear());
        return ! holidays.contains(date);
    }
}