package com.jw.holidayguard.service.materialization.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.ScheduleRules;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GREEN: Handler for MONTHLY_PATTERN rule type.
 * Processes JSON patterns:
 * - {"dayOfMonth": 15, "skipWeekends": true}
 * - {"dayOfWeek": "FRIDAY", "weekOfMonth": "LAST"}
 */
@Component
public class MonthlyPatternHandler implements RuleHandler {

    private final ObjectMapper objectMapper;

    public MonthlyPatternHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LocalDate> generateDates(ScheduleRules rule, LocalDate fromDate, LocalDate toDate) {
        // Handle invalid date range
        if (fromDate.isAfter(toDate)) {
            return Collections.emptyList();
        }
        
        String jsonConfig = rule.getRuleConfig();
        if (jsonConfig == null || jsonConfig.trim().isEmpty()) {
            throw new IllegalArgumentException("Monthly pattern JSON configuration cannot be null or empty");
        }
        
        try {
            JsonNode config = objectMapper.readTree(jsonConfig);
            return generateMonthlyDates(config, fromDate, toDate);
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format in monthly pattern configuration: " + jsonConfig, e);
        }
    }
    
    private List<LocalDate> generateMonthlyDates(JsonNode config, LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> dates = new ArrayList<>();
        
        LocalDate current = fromDate.withDayOfMonth(1); // Start at first day of fromDate month
        
        while (current.isBefore(toDate.plusMonths(1))) { // Go through each month
            LocalDate candidateDate = null;
            
            if (config.has("dayOfMonth")) {
                candidateDate = generateDayOfMonth(config, current);
            } else if (config.has("dayOfWeek") && config.has("weekOfMonth")) {
                candidateDate = generateDayOfWeekInMonth(config, current);
            } else {
                throw new IllegalArgumentException("Monthly pattern must specify either 'dayOfMonth' or both 'dayOfWeek' and 'weekOfMonth'");
            }
            
            // Apply weekend skipping if requested
            if (candidateDate != null && config.has("skipWeekends") && config.get("skipWeekends").asBoolean()) {
                candidateDate = skipWeekends(candidateDate);
            }
            
            // Add if within date range
            if (candidateDate != null && 
                !candidateDate.isBefore(fromDate) && 
                !candidateDate.isAfter(toDate)) {
                dates.add(candidateDate);
            }
            
            current = current.plusMonths(1);
        }
        
        return dates;
    }
    
    private LocalDate generateDayOfMonth(JsonNode config, LocalDate monthStart) {
        int dayOfMonth = config.get("dayOfMonth").asInt();
        
        try {
            return monthStart.withDayOfMonth(dayOfMonth);
        } catch (Exception e) {
            // Day doesn't exist in this month (e.g., Feb 31)
            return null;
        }
    }
    
    private LocalDate generateDayOfWeekInMonth(JsonNode config, LocalDate monthStart) {
        String dayOfWeekStr = config.get("dayOfWeek").asText();
        String weekOfMonthStr = config.get("weekOfMonth").asText();
        
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid dayOfWeek: " + dayOfWeekStr + ". Must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
        }
        
        switch (weekOfMonthStr.toUpperCase()) {
            case "FIRST":
                return monthStart.with(TemporalAdjusters.firstInMonth(dayOfWeek));
            case "SECOND":
                return monthStart.with(TemporalAdjusters.dayOfWeekInMonth(2, dayOfWeek));
            case "THIRD":
                return monthStart.with(TemporalAdjusters.dayOfWeekInMonth(3, dayOfWeek));
            case "FOURTH":
                return monthStart.with(TemporalAdjusters.dayOfWeekInMonth(4, dayOfWeek));
            case "LAST":
                return monthStart.with(TemporalAdjusters.lastInMonth(dayOfWeek));
            default:
                throw new IllegalArgumentException("Invalid weekOfMonth: " + weekOfMonthStr + ". Must be one of: FIRST, SECOND, THIRD, FOURTH, LAST");
        }
    }
    
    private LocalDate skipWeekends(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2); // Move to Monday
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1); // Move to Monday
        }
        return date; // Already a weekday
    }
    
    @Override
    public ScheduleRules.RuleType getSupportedRuleType() {
        return ScheduleRules.RuleType.MONTHLY_PATTERN;
    }
}