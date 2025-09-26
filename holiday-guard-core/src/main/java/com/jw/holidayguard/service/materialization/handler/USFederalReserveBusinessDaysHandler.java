package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.util.ACHProcessingScheduleFactory.USFederalHolidays;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class USFederalReserveBusinessDaysHandler implements RuleHandler {

    @Override
    public ScheduleRule.RuleType getSupportedRuleType() {
        return ScheduleRule.RuleType.US_FEDERAL_RESERVE_BUSINESS_DAYS;
    }

    @Override
    public List<LocalDate> generateDates(ScheduleRule rule, LocalDate fromDate, LocalDate toDate) {
        return fromDate.datesUntil(toDate.plusDays(1))
                .filter(this::isBusinessDay)
                .collect(Collectors.toList());
    }

    @Override
    public boolean shouldRun(ScheduleRule rule, LocalDate date) {
        return isBusinessDay(date);
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }

        List<LocalDate> holidays = USFederalHolidays.getHolidays(date.getYear());
        return !holidays.contains(date);
    }
}