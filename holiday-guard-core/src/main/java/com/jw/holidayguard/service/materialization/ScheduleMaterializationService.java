package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;

/**
 * GREEN: Core service for materializing schedule calendars from rules and overrides.
 * Combines rule engine results with override application and persists to database.
 */
@Service
public class ScheduleMaterializationService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleVersionRepository scheduleVersionRepository;
    private final ScheduleRuleRepository scheduleRuleRepository;
    private final ScheduleMaterializedCalendarRepository materializedCalendarRepository;
    private final RuleEngine ruleEngine;
    private final OverrideApplicator overrideApplicator;

    public ScheduleMaterializationService(
            ScheduleRepository scheduleRepository,
            ScheduleVersionRepository scheduleVersionRepository,
            ScheduleRuleRepository scheduleRuleRepository,
            ScheduleMaterializedCalendarRepository materializedCalendarRepository,
            RuleEngine ruleEngine,
            OverrideApplicator overrideApplicator) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleVersionRepository = scheduleVersionRepository;
        this.scheduleRuleRepository = scheduleRuleRepository;
        this.materializedCalendarRepository = materializedCalendarRepository;
        this.ruleEngine = ruleEngine;
        this.overrideApplicator = overrideApplicator;
    }

    /**
     * Materializes the schedule calendar for the given date range.
     * Combines all active rules and applies overrides to generate the final calendar.
     */
    @Transactional
    public List<LocalDate> materializeCalendar(UUID scheduleId, LocalDate fromDate, LocalDate toDate) {

        // verify schedule exists
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // and has active version
        ScheduleVersion activeVersion = scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("No active version found for schedule: " + scheduleId));

        // get the rule for that version
        ScheduleRule activeRule = scheduleRuleRepository.findByScheduleIdAndVersionIdAndActiveTrue(scheduleId, activeVersion.getId())
                .orElseThrow(() -> new IllegalStateException("Schedule has no active rule"));

        // generate business days from rule
        List<LocalDate> ruleDates = ruleEngine.generateDates(activeRule, fromDate, toDate);

        // Apply overrides
        List<LocalDate> finalDates = overrideApplicator.applyOverrides(scheduleId, activeVersion.getId(), ruleDates, fromDate, toDate);

        // Clear existing materialized entries in the date range and save new ones
        materializedCalendarRepository.deleteByScheduleIdAndVersionIdAndOccursOnBetween(scheduleId, activeVersion.getId(), fromDate, toDate);

        for (LocalDate date : finalDates) {
            ScheduleMaterializedCalendar entry = ScheduleMaterializedCalendar.builder()
                    .scheduleId(scheduleId)
                    .versionId(activeVersion.getId())
                    .occursOn(date)
                    .status(ScheduleMaterializedCalendar.OccurrenceStatus.SCHEDULED)
                    .build();

            materializedCalendarRepository.save(entry);
        }

        return finalDates;
    }
}
