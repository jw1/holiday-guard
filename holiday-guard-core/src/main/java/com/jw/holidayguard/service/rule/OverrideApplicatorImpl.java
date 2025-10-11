package com.jw.holidayguard.service.rule;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.repository.DeviationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * GREEN: Implementation of OverrideApplicator that applies FORCE_SKIP and FORCE_RUN overrides.
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
            .filter(o -> o.getVersionId().equals(versionId) && !o.getDeviationDate().isBefore(fromDate) && !o.getDeviationDate().isAfter(toDate))
            .toList();

        if (activeOverrides.isEmpty()) {
            return new ArrayList<>(ruleDates);
        }

        // Use LinkedHashSet to maintain order and avoid duplicates
        Set<LocalDate> finalDates = new LinkedHashSet<>(ruleDates);

        // Apply each override
        for (Deviation override : activeOverrides) {
            LocalDate overrideDate = override.getDeviationDate();

            switch (override.getAction()) {
                case FORCE_SKIP:
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