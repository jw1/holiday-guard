package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.dto.DailyScheduleStatusDto;
import com.jw.holidayguard.dto.QueryLogDto;
import com.jw.holidayguard.dto.request.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.response.ShouldRunQueryResponse;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.QueryLogRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.service.rule.RuleEngine;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleQueryService {

    private final ScheduleRepository scheduleRepository;
    private final VersionRepository versionRepository;
    private final DeviationRepository overrideRepository;
    private final QueryLogRepository queryLogRepository;
    private final RuleEngine ruleEngine;
    private final RuleRepository ruleRepository;

    public ScheduleQueryService(
            ScheduleRepository scheduleRepository,
            VersionRepository versionRepository,
            DeviationRepository overrideRepository,
            QueryLogRepository queryLogRepository,
            RuleEngine ruleEngine,
            RuleRepository ruleRepository) {
        this.scheduleRepository = scheduleRepository;
        this.versionRepository = versionRepository;
        this.overrideRepository = overrideRepository;
        this.queryLogRepository = queryLogRepository;
        this.ruleEngine = ruleEngine;
        this.ruleRepository = ruleRepository;
    }

    public List<QueryLogDto> findAllLogs() {
        // 1. Fetch all logs, sorted by newest first
        List<QueryLog> logs = queryLogRepository.findAll(Sort.by(Sort.Direction.DESC, "queriedAt"));

        // 2. Get unique schedule IDs
        var scheduleIds = logs.stream()
                .map(QueryLog::getScheduleId)
                .collect(Collectors.toSet());

        // 3. Fetch all schedules in a single batch query
        Map<Long, String> scheduleNames = scheduleRepository.findAllById(scheduleIds).stream()
                .collect(Collectors.toMap(Schedule::getId, Schedule::getName));

        // 4. Map to DTOs
        return logs.stream()
                .map(log -> new QueryLogDto(
                        log.getId(),
                        log.getScheduleId(),
                        scheduleNames.getOrDefault(log.getScheduleId(), "Unknown"),
                        log.getVersionId(),
                        log.getQueryDate(),
                        log.isShouldRunResult(),
                        log.getReason(),
                        log.isDeviationApplied(),
                        log.getClientIdentifier(),
                        log.getQueriedAt()
                ))
                .toList();
    }

    public long getTotalSchedulesCount() {
        return scheduleRepository.count();
    }

    public long getActiveSchedulesCount() {
        return scheduleRepository.countByActive(true);
    }

    public List<DailyScheduleStatusDto> getDailyRunStatusForAllActiveSchedules() {
        List<Schedule> activeSchedules = scheduleRepository.findByActiveTrue();
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(LocalDate.now(), "internal-dashboard");

        return activeSchedules.stream()
                .map(schedule -> {
                    var response = shouldRunToday(schedule.getId(), request);
                    return new DailyScheduleStatusDto(
                            schedule.getId(),
                            schedule.getName(),
                            response.isShouldRun(),
                            response.getReason()
                    );
                })
                .collect(Collectors.toList());
    }

    public ShouldRunQueryResponse shouldRunToday(Long scheduleId, ShouldRunQueryRequest request) {
        // Validate schedule exists and is active
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        if (!schedule.isActive()) {
            throw new IllegalArgumentException("Schedule is not active: " + scheduleId);
        }

        // Validate date bounds (reasonable planning horizon)
        LocalDate queryDate = request.getQueryDate(); // This defaults to today if null
        LocalDate today = LocalDate.now();
        LocalDate maxFutureDate = today.plusYears(5); // 5 year planning horizon
        LocalDate minPastDate = today.minusYears(1);  // 1 year historical data

        if (queryDate.isAfter(maxFutureDate)) {
            throw new IllegalArgumentException("Query date too far in future: " + queryDate + " (max: " + maxFutureDate + ")");
        }
        if (queryDate.isBefore(minPastDate)) {
            throw new IllegalArgumentException("Query date too far in past: " + queryDate + " (min: " + minPastDate + ")");
        }

        // Get active version
        Version activeVersion = versionRepository.findByScheduleIdAndActiveTrue(scheduleId)
            .orElseThrow(() -> new IllegalStateException("No active version found for schedule: " + scheduleId));

        // Fetch rule for active version
        Optional<Rule> ruleOpt = ruleRepository.findByVersionId(activeVersion.getId());
        if (ruleOpt.isEmpty()) {
            throw new IllegalStateException("No rule found for version: " + activeVersion.getId());
        }

        // Fetch deviations for active version
        List<Deviation> deviations = overrideRepository.findByScheduleId(scheduleId)
            .stream()
            .filter(d -> d.getVersionId().equals(activeVersion.getId()))
            .toList();

        // Create Calendar abstraction - this encapsulates the shouldRun business logic
        // RuleEngine implements Calendar.RuleEvaluator, so we can use it directly
        Calendar calendar = new Calendar(schedule, ruleOpt.get(), deviations, ruleEngine::shouldRun);

        // Delegate to Calendar for shouldRun decision (handles deviations + rule evaluation)
        boolean shouldRun = calendar.shouldRun(queryDate);

        // Determine if deviation was applied (check if any deviation exists for this date)
        boolean deviationApplied = deviations.stream()
                .anyMatch(d -> d.getOverrideDate().equals(queryDate));

        // Build reason message
        String reason;
        if (deviationApplied) {
            Deviation deviation = deviations.stream()
                    .filter(d -> d.getOverrideDate().equals(queryDate))
                    .findFirst()
                    .orElseThrow();
            reason = "Override applied: " + deviation.getReason();
        } else {
            reason = shouldRun
                    ? "Scheduled to run - rule matches"
                    : "Not scheduled to run - rule does not match";
        }

        // Log the query for audit trail
        QueryLog queryLog = QueryLog.builder()
            .scheduleId(scheduleId)
            .versionId(activeVersion.getId())
            .queryDate(queryDate)
            .shouldRunResult(shouldRun)
            .reason(reason)
            .deviationApplied(deviationApplied)
            .clientIdentifier(request.getClientIdentifier())
            .build();

        queryLogRepository.save(queryLog);

        // Return response
        return new ShouldRunQueryResponse(
            scheduleId,
            queryDate,
            shouldRun,
            reason,
            deviationApplied,
            activeVersion.getId()
        );
    }
}