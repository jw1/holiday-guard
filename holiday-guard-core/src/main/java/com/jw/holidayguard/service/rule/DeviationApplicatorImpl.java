package com.jw.holidayguard.service.rule;
import com.google.common.collect.Range;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.repository.DeviationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of DeviationApplicator that applies FORCE_SKIP and FORCE_RUN overrides.
 * Handles deviation precedence and maintains chronological order.
 */
@Component
public class DeviationApplicatorImpl implements DeviationApplicator {

    private final DeviationRepository deviationRepository;

    public DeviationApplicatorImpl(DeviationRepository deviationRepository) {
        this.deviationRepository = deviationRepository;
    }

    @Override
    public List<LocalDate> applyDeviations(Long scheduleId, Long versionId, List<LocalDate> ruleDates, LocalDate from, LocalDate to) {

        // get all deviations in date range
        var range = Range.closed(from, to);
        List<Deviation> deviations = deviationRepository.findByScheduleId(scheduleId)
            .stream()
            .filter(d -> Objects.equals(d.getScheduleId(), scheduleId))
            .filter(d -> range.contains(d.getDeviationDate()))
            .toList();

        // no deviations?  just skip to end
        if (deviations.isEmpty()) return new ArrayList<>(ruleDates);

        // Use LinkedHashSet to maintain order and avoid duplicates
        Set<LocalDate> finalDates = applyDeviations(ruleDates, deviations);

        // Convert back to list and sort
        return finalDates.stream()
            .sorted()
            .toList();
    }

    private static Set<LocalDate> applyDeviations(List<LocalDate> in, List<Deviation> deviations) {

        Set<LocalDate> out = new LinkedHashSet<>(in);

        // Apply each deviation
        for (Deviation deviation : deviations) {

            LocalDate date = deviation.getDeviationDate();

            switch (deviation.getAction()) {
                case FORCE_SKIP:
                    // Remove the date if it exists
                    out.remove(date);
                    break;

                case FORCE_RUN:
                    // Add the date (Set will handle duplicates)
                    out.add(date);
                    break;
            }
        }

        return out;
    }
}