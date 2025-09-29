package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.dto.DailyScheduleStatusDto;
import com.jw.holidayguard.dto.ScheduleQueryLogDto;
import com.jw.holidayguard.dto.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.ShouldRunQueryResponse;
import com.jw.holidayguard.repository.ScheduleOverrideRepository;
import com.jw.holidayguard.repository.ScheduleQueryLogRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
import com.jw.holidayguard.repository.ScheduleRuleRepository;
import com.jw.holidayguard.service.materialization.RuleEngine;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleQueryService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleVersionRepository scheduleVersionRepository;
    private final ScheduleOverrideRepository overrideRepository;
    private final ScheduleQueryLogRepository queryLogRepository;
    private final RuleEngine ruleEngine;
    private final ScheduleRuleRepository scheduleRuleRepository;

    public ScheduleQueryService(
            ScheduleRepository scheduleRepository,
            ScheduleVersionRepository scheduleVersionRepository,
            ScheduleOverrideRepository overrideRepository,
            ScheduleQueryLogRepository queryLogRepository,
            RuleEngine ruleEngine,
            ScheduleRuleRepository scheduleRuleRepository) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleVersionRepository = scheduleVersionRepository;
        this.overrideRepository = overrideRepository;
        this.queryLogRepository = queryLogRepository;
        this.ruleEngine = ruleEngine;
        this.scheduleRuleRepository = scheduleRuleRepository;
    }

    public List<ScheduleQueryLogDto> findAllLogs() {
        // 1. Fetch all logs, sorted by newest first
        List<ScheduleQueryLog> logs = queryLogRepository.findAll(Sort.by(Sort.Direction.DESC, "queriedAt"));

        // 2. Get unique schedule IDs
        var scheduleIds = logs.stream()
                .map(ScheduleQueryLog::getScheduleId)
                .collect(Collectors.toSet());

        // 3. Fetch all schedules in a single batch query
        Map<UUID, String> scheduleNames = scheduleRepository.findAllById(scheduleIds).stream()
                .collect(Collectors.toMap(Schedule::getId, Schedule::getName));

        // 4. Map to DTOs
        return logs.stream()
                .map(log -> new ScheduleQueryLogDto(
                        log.getId(),
                        log.getScheduleId(),
                        scheduleNames.getOrDefault(log.getScheduleId(), "Unknown"),
                        log.getVersionId(),
                        log.getQueryDate(),
                        log.isShouldRunResult(),
                        log.getReason(),
                        log.isOverrideApplied(),
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

    public ShouldRunQueryResponse shouldRunToday(UUID scheduleId, ShouldRunQueryRequest request) {
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
        ScheduleVersion activeVersion = scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId)
            .orElseThrow(() -> new IllegalStateException("No active version found for schedule: " + scheduleId));
        
        // Check for overrides first (they take precedence)
        Optional<ScheduleOverride> override = overrideRepository.findByScheduleId(scheduleId)
            .stream()
            .filter(o -> o.getVersionId().equals(activeVersion.getId()) && o.getOverrideDate().equals(queryDate))
            .findFirst();
        
        boolean shouldRun;
        String reason;
        boolean overrideApplied = false;
        
        if (override.isPresent()) {
            // Override exists - apply it
            overrideApplied = true;
            ScheduleOverride overrideRule = override.get();
            shouldRun = overrideRule.getAction() == ScheduleOverride.OverrideAction.FORCE_RUN;
            reason = "Override applied: " + overrideRule.getReason();
        } else {
            // No override - evaluate rules
            Optional<ScheduleRule> rule = scheduleRuleRepository.findByVersionId(activeVersion.getId());
            if (rule.isEmpty()) {
                shouldRun = false;
                reason = "Not scheduled to run - no rules found for version";
            } else {
                shouldRun = ruleEngine.shouldRun(rule.get(), queryDate);
                
                if (shouldRun) {
                    reason = "Scheduled to run - rule matches";
                } else {
                    reason = "Not scheduled to run - rule does not match";
                }
            }
        }
        
        // Log the query for audit trail
        ScheduleQueryLog queryLog = ScheduleQueryLog.builder()
            .scheduleId(scheduleId)
            .versionId(activeVersion.getId())
            .queryDate(queryDate)
            .shouldRunResult(shouldRun)
            .reason(reason)
            .overrideApplied(overrideApplied)
            .clientIdentifier(request.getClientIdentifier())
            .build();
        
        queryLogRepository.save(queryLog);
        
        // Return response
        return new ShouldRunQueryResponse(
            scheduleId,
            queryDate,
            shouldRun,
            reason,
            overrideApplied,
            activeVersion.getId()
        );
    }
}