package com.jw.holidayguard.service.materialization.handler;

import com.jw.holidayguard.domain.ScheduleRules;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GREEN: Handler for WEEKDAYS_ONLY rule type.
 * Generates Monday through Friday dates, excluding weekends.
 */
@Component
public class WeekdaysOnlyHandler implements RuleHandler {

    @Override
    public List<LocalDate> generateDates(ScheduleRules rule, LocalDate fromDate, LocalDate toDate) {
        // Handle invalid date range
        if (fromDate.isAfter(toDate)) {
            return Collections.emptyList();
        }
        
        List<LocalDate> weekdays = new ArrayList<>();
        LocalDate current = fromDate;
        
        while (!current.isAfter(toDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                weekdays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return weekdays;
    }
    
    @Override
    public ScheduleRules.RuleType getSupportedRuleType() {
        return ScheduleRules.RuleType.WEEKDAYS_ONLY;
    }
}