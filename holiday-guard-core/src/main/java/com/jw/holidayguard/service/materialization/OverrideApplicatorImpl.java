package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.repository.DeviationRepository;
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

    private final DeviationRepository deviationRepository;

    public OverrideApplicatorImpl(DeviationRepository deviationRepository) {
        this.deviationRepository = deviationRepository;
    }

    @Override
    public List<LocalDate> applyOverrides(Long scheduleId, Long versionId, List<LocalDate> ruleDates, LocalDate fromDate, LocalDate toDate) {
        // Get all active overrides in the date range
        List<Deviation> activeOverrides = deviationRepository.findByScheduleId(scheduleId)
            .stream()
            .filter(o -> o.getVersionId().equals(versionId) && !o.getOverrideDate().isBefore(fromDate) && !o.getOverrideDate().isAfter(toDate))
            .toList();
        
        if (activeOverrides.isEmpty()) {
            return new ArrayList<>(ruleDates);
        }
        
        // Use LinkedHashSet to maintain order and avoid duplicates
        Set<LocalDate> finalDates = new LinkedHashSet<>(ruleDates);
        
        // Apply each override
        for (Deviation override : activeOverrides) {
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