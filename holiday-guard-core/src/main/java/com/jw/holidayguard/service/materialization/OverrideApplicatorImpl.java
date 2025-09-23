package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.ScheduleOverride;
import com.jw.holidayguard.repository.ScheduleOverrideRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * GREEN: Implementation of OverrideApplicator that applies SKIP and FORCE_RUN overrides.
 * Handles override precedence and maintains chronological order.
 */
@Component
public class OverrideApplicatorImpl implements OverrideApplicator {

    private final ScheduleOverrideRepository scheduleOverrideRepository;

    public OverrideApplicatorImpl(ScheduleOverrideRepository scheduleOverrideRepository) {
        this.scheduleOverrideRepository = scheduleOverrideRepository;
    }

    @Override
    public List<LocalDate> applyOverrides(UUID scheduleId, UUID versionId, List<LocalDate> ruleDates, LocalDate fromDate, LocalDate toDate) {
        // Get all active overrides in the date range
        List<ScheduleOverride> activeOverrides = scheduleOverrideRepository.findByScheduleId(scheduleId)
            .stream()
            .filter(o -> o.getVersionId().equals(versionId) && !o.getOverrideDate().isBefore(fromDate) && !o.getOverrideDate().isAfter(toDate))
            .toList();
        
        if (activeOverrides.isEmpty()) {
            return new ArrayList<>(ruleDates);
        }
        
        // Use LinkedHashSet to maintain order and avoid duplicates
        Set<LocalDate> finalDates = new LinkedHashSet<>(ruleDates);
        
        // Apply each override
        for (ScheduleOverride override : activeOverrides) {
            LocalDate overrideDate = override.getOverrideDate();
            
            switch (override.getAction()) {
                case SKIP:
                    // Remove the date if it exists
                    finalDates.remove(overrideDate);
                    break;
                    
                case FORCE_RUN:
                    // Add the date (Set will handle duplicates)
                    finalDates.add(overrideDate);
                    break;
            }
        }
        
        // Convert back to list and sort
        return finalDates.stream()
            .sorted()
            .toList();
    }
}