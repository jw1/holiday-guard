package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.util.ACHProcessingScheduleFactory;
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
        List<LocalDate> weekdays = fromDate.datesUntil(toDate.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());

        List<LocalDate> holidays = ACHProcessingScheduleFactory.USFederalHolidays.getHolidays(fromDate.getYear());
        if (fromDate.getYear() != toDate.getYear()) {
            holidays.addAll(ACHProcessingScheduleFactory.USFederalHolidays.getHolidays(toDate.getYear()));
        }

        return weekdays.stream()
                .filter(d -> !holidays.contains(d))
                .collect(Collectors.toList());
    }
}