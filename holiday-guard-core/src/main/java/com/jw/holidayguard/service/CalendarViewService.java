package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Calendar;
import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.RunStatus;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.CalendarDayDto;
import com.jw.holidayguard.dto.MultiScheduleCalendarDto;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import com.jw.holidayguard.service.rule.RuleEngine;
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
        LocalDate fromDate = yearMonth.atDay(1);
        LocalDate toDate = yearMonth.atEndOfMonth();

        for (Long scheduleId : scheduleIds) {
            // Fetch schedule
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                log.warn("Schedule {} not found, skipping", scheduleId);
                continue;
            }

            Schedule schedule = scheduleOpt.get();

            // Get active version
            Optional<Version> versionOpt = versionRepository.findByScheduleIdAndActiveTrue(scheduleId);
            if (versionOpt.isEmpty()) {
                log.warn("No active version for schedule {}, skipping", scheduleId);
                continue;
            }

            Version version = versionOpt.get();

            // Fetch rule for active version
            Optional<Rule> ruleOpt = ruleRepository.findByVersionId(version.getId());
            if (ruleOpt.isEmpty()) {
                log.warn("No rule found for version {}, skipping", scheduleId);
                continue;
            }

            Rule rule = ruleOpt.get();

            // Fetch deviations (if requested)
            List<Deviation> deviations = includeDeviations
                    ? deviationRepository.findByScheduleIdAndVersionId(scheduleId, version.getId())
                    : List.of();

            // Create Calendar abstraction - encapsulates shouldRun business logic
            // RuleEngine implements Calendar.RuleEvaluator, so we can use it directly
            Calendar calendar = new Calendar(schedule, rule, deviations, ruleEngine::shouldRun);

            // Query date range using Calendar - guarantees algorithm consistency
            Map<LocalDate, Boolean> shouldRunMap = calendar.shouldRun(fromDate, toDate);

            // Convert to DTOs
            for (Map.Entry<LocalDate, Boolean> entry : shouldRunMap.entrySet()) {
                LocalDate date = entry.getKey();
                boolean shouldRun = entry.getValue();

                // Find deviation for this date (if applicable) to get reason
                Optional<Deviation> deviationOpt = deviations.stream()
                        .filter(d -> d.getDeviationDate().equals(date))
                        .findFirst();

                // calculate RunStatus and a reason
                RunStatus status = deviationOpt
                        .map(d -> RunStatus.fromCalendar(shouldRun, d))
                        .orElse(RunStatus.fromCalendar(shouldRun));

                String reason = deviationOpt
                        .map(Deviation::getReason)
                        .orElse(null);

                CalendarDayDto dayDto = new CalendarDayDto(
                        scheduleId,
                        schedule.getName(),
                        date,
                        status,
                        reason);

                allDays.add(dayDto);
            }
        }

        return new MultiScheduleCalendarDto(yearMonth, allDays);
    }
}
