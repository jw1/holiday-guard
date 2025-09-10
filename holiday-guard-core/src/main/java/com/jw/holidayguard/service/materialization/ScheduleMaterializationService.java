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
    private final ScheduleRulesRepository scheduleRulesRepository;
    private final ScheduleMaterializedCalendarRepository materializedCalendarRepository;
    private final RuleEngine ruleEngine;
    private final OverrideApplicator overrideApplicator;

    public ScheduleMaterializationService(
            ScheduleRepository scheduleRepository,
            ScheduleVersionRepository scheduleVersionRepository,
            ScheduleRulesRepository scheduleRulesRepository,
            ScheduleMaterializedCalendarRepository materializedCalendarRepository,
            RuleEngine ruleEngine,
            OverrideApplicator overrideApplicator) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleVersionRepository = scheduleVersionRepository;
        this.scheduleRulesRepository = scheduleRulesRepository;
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
        // Validate schedule exists
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // Get active version
        ScheduleVersion activeVersion = scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("No active version found for schedule: " + scheduleId));

        // Get all active rules for this version
        List<ScheduleRules> activeRules = scheduleRulesRepository.findByScheduleIdAndVersionIdAndActiveTrue(scheduleId, activeVersion.getId());

        // Generate dates from all rules and combine them
        Set<LocalDate> allRuleDates = new LinkedHashSet<>(); // Use Set to avoid duplicates, LinkedHashSet to maintain order
        
        for (ScheduleRules rule : activeRules) {
            List<LocalDate> ruleDates = ruleEngine.generateDates(rule, fromDate, toDate);
            allRuleDates.addAll(ruleDates);
        }

        // Convert back to sorted list
        List<LocalDate> combinedRuleDates = allRuleDates.stream()
            .sorted()
            .collect(Collectors.toList());

        // Apply overrides
        List<LocalDate> finalDates = overrideApplicator.applyOverrides(scheduleId, activeVersion.getId(), combinedRuleDates, fromDate, toDate);

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