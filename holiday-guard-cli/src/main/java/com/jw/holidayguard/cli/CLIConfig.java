package com.jw.holidayguard.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration structure for CLI-based schedules loaded from JSON.
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "schedules": [
 *     {
 *       "name": "Payroll Schedule",
 *       "description": "US payroll processing calendar",
 *       "rule": {
 *         "ruleType": "WEEKDAYS_ONLY"
 *       },
 *       "deviations": [
 *         {
 *           "date": "2025-12-25",
 *           "action": "FORCE_SKIP",
 *           "reason": "Christmas Day"
 *         }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 */
@Data
public class CLIConfig {

    private List<ScheduleConfig> schedules = new ArrayList<>();

    /**
     * Find a schedule by name (case-insensitive).
     */
    public ScheduleConfig findSchedule(String name) {
        return schedules.stream()
            .filter(s -> s.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get list of all schedule names for error messages.
     */
    public List<String> getScheduleNames() {
        return schedules.stream()
            .map(ScheduleConfig::getName)
            .collect(Collectors.toList());
    }

    @Data
    public static class ScheduleConfig {
        private String name;
        private String description;
        private RuleConfig rule;
        private List<DeviationConfig> deviations = new ArrayList<>();
    }

    @Data
    public static class RuleConfig {
        @JsonProperty("ruleType")
        private String ruleType;

        @JsonProperty("ruleConfig")
        private String ruleConfig;
    }

    @Data
    public static class DeviationConfig {
        private LocalDate date;
        private String action; // FORCE_RUN or FORCE_SKIP
        private String reason;
    }
}
