package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.CalendarDayDto;
import com.jw.holidayguard.dto.MultiScheduleCalendarDto;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import com.jw.holidayguard.service.materialization.RuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for generating multi-schedule calendar views.
 * Aggregates calendar data from multiple schedules with deviations applied.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarViewService {

    private final ScheduleRepository scheduleRepository;
    private final RuleRepository ruleRepository;
    private final VersionRepository versionRepository;
    private final DeviationRepository deviationRepository;
    private final RuleEngine ruleEngine;

    /**
     * Get calendar data for multiple schedules for a given month.
     * Includes deviation overlays if includeDeviations is true.
     *
     * @param scheduleIds List of schedule IDs to include
     * @param yearMonth The month to generate calendar for
     * @param includeDeviations Whether to apply deviations to the calendar
     * @return MultiScheduleCalendarDto containing all calendar days
     */
    public MultiScheduleCalendarDto getMultiScheduleCalendar(
            List<Long> scheduleIds,
            YearMonth yearMonth,
            boolean includeDeviations) {

        List<CalendarDayDto> allDays = new ArrayList<>();

        for (Long scheduleId : scheduleIds) {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                log.warn("Schedule {} not found, skipping", scheduleId);
                continue;
            }

            Schedule schedule = scheduleOpt.get();
            Optional<Rule> ruleOpt = ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId);

            if (ruleOpt.isEmpty()) {
                log.warn("No active rule found for schedule {}, skipping", scheduleId);
                continue;
            }

            Rule rule = ruleOpt.get();
            LocalDate fromDate = yearMonth.atDay(1);
            LocalDate toDate = yearMonth.atEndOfMonth();

            // Generate base calendar run dates
            List<LocalDate> runDates = ruleEngine.generateDates(rule, fromDate, toDate);

            // Get deviations if requested
            Map<LocalDate, Deviation> deviationMap = Map.of();
            if (includeDeviations) {
                Optional<Version> activeVersion = versionRepository.findByScheduleIdAndActiveTrue(scheduleId);
                if (activeVersion.isPresent()) {
                    deviationMap = deviationRepository
                            .findByScheduleIdAndVersionId(scheduleId, activeVersion.get().getId())
                            .stream()
                            .collect(Collectors.toMap(Deviation::getOverrideDate, d -> d));
                }
            }

            // Create calendar days for the entire month
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate currentDate = yearMonth.atDay(day);
                String status;
                String reason = null;

                // Check for deviations first
                if (deviationMap.containsKey(currentDate)) {
                    Deviation deviation = deviationMap.get(currentDate);
                    status = deviation.getAction().name(); // "FORCE_RUN" or "SKIP"
                    reason = deviation.getReason();
                } else {
                    // Use base schedule
                    status = runDates.contains(currentDate) ? "run" : "no-run";
                }

                CalendarDayDto dayDto = new CalendarDayDto(
                        scheduleId,
                        schedule.getName(),
                        currentDate,
                        status,
                        reason
                );
                allDays.add(dayDto);
            }
        }

        return new MultiScheduleCalendarDto(yearMonth, allDays);
    }
}
