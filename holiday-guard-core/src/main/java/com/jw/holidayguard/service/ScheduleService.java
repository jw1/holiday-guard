package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.CreateScheduleRequest;
import com.jw.holidayguard.dto.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleRuleRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
import com.jw.holidayguard.dto.ScheduleCalendarDto;
import com.jw.holidayguard.dto.ScheduleOverrideDto;
import com.jw.holidayguard.repository.ScheduleOverrideRepository;
import com.jw.holidayguard.service.CurrentUserService;
import com.jw.holidayguard.service.materialization.RuleEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository repository;
    private final ScheduleRuleRepository ruleRepository;
    private final ScheduleVersionRepository versionRepository;
    private final ScheduleOverrideRepository overrideRepository;
    private final RuleEngine ruleEngine;
    private final CurrentUserService currentUserService;

    public ScheduleService(ScheduleRepository repository, ScheduleRuleRepository ruleRepository, ScheduleVersionRepository versionRepository, ScheduleOverrideRepository overrideRepository, RuleEngine ruleEngine, CurrentUserService currentUserService) {
        this.repository = repository;
        this.ruleRepository = ruleRepository;
        this.versionRepository = versionRepository;
        this.overrideRepository = overrideRepository;
        this.ruleEngine = ruleEngine;
        this.currentUserService = currentUserService;
    }

    public Schedule createSchedule(CreateScheduleRequest request) {
        // Check for duplicate name
        if (repository.findByName(request.getName()).isPresent()) {
            throw new DuplicateScheduleException(request.getName());
        }

        String currentUser = currentUserService.getCurrentUsername();

        Schedule schedule = Schedule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry())
                .active(request.isActive())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        Schedule savedSchedule = repository.save(schedule);

        ScheduleVersion version = ScheduleVersion.builder()
                .scheduleId(savedSchedule.getId())
                .active(true)
                .build();

        ScheduleVersion savedVersion = versionRepository.save(version);

        ScheduleRule rule = ScheduleRule.builder()
                .scheduleId(savedSchedule.getId())
                .versionId(savedVersion.getId())
                .ruleType(ScheduleRule.RuleType.valueOf(request.getRuleType()))
                .ruleConfig(request.getRuleConfig())
                .build();

        ruleRepository.save(rule);

        return savedSchedule;
    }

    @Transactional(readOnly = true)
    public Schedule findScheduleById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Schedule findScheduleByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ScheduleNotFoundException(name));
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllActiveSchedules() {
        return repository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Schedule> findAllSchedules() {
        return repository.findAll();
    }

    public Schedule updateSchedule(UUID id, UpdateScheduleRequest updateData) {
        // Find existing schedule (throws exception if not found)
        Schedule existing = findScheduleById(id);

        // Check for name conflicts only if name is being changed
        if (updateData.getName() != null && !updateData.getName().equals(existing.getName())) {
            if (repository.findByName(updateData.getName()).isPresent()) {
                throw new DuplicateScheduleException(updateData.getName());
            }
        }

        // Update fields using the managed entity for automatic JPA change tracking
        if (updateData.getName() != null) {
            existing.setName(updateData.getName());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getCountry() != null) {
            existing.setCountry(updateData.getCountry());
        }

        if (updateData.getActive() != null) {
            if (updateData.getActive()) {
                existing.setActive(true);

            } else {
                existing.setActive(false);

            }
        }

        if (updateData.getRuleType() != null) {
            Optional<ScheduleRule> latestRuleOpt = findLatestRuleForSchedule(id);
            boolean ruleChanged = latestRuleOpt
                    .map(latestRule ->
                            !latestRule.getRuleType().name().equals(updateData.getRuleType()) ||
                                    !latestRule.getRuleConfig().equals(updateData.getRuleConfig()))
                    .orElse(true); // If no rule exists, it has changed

            if (ruleChanged) {
                // Deactivate old version
                versionRepository.findByScheduleIdAndActiveTrue(id).ifPresent(v -> v.setActive(false));

                // Create new version
                ScheduleVersion version = ScheduleVersion.builder()
                        .scheduleId(id)
                        .active(true)
                        .build();
                ScheduleVersion savedVersion = versionRepository.save(version);

                // Create new rule
                ScheduleRule rule = ScheduleRule.builder()
                        .scheduleId(id)
                        .versionId(savedVersion.getId())
                        .ruleType(ScheduleRule.RuleType.valueOf(updateData.getRuleType()))
                        .ruleConfig(updateData.getRuleConfig())
                        .build();
                ruleRepository.save(rule);
            }
        }

        existing.setUpdatedBy(currentUserService.getCurrentUsername());

        // JPA automatically detects changes and updates on transaction commit
        return existing;
    }


    @Transactional(readOnly = true)
    public Optional<ScheduleRule> findLatestRuleForSchedule(UUID scheduleId) {
        return ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId);
    }

    @Transactional(readOnly = true)
    public ScheduleCalendarDto getScheduleCalendar(UUID scheduleId, YearMonth yearMonth) {
        Schedule schedule = findScheduleById(scheduleId);
        ScheduleRule rule = findLatestRuleForSchedule(scheduleId)
                .orElseThrow(() -> new IllegalStateException("Schedule has no rules"));

        LocalDate fromDate = yearMonth.atDay(1);
        LocalDate toDate = yearMonth.atEndOfMonth();

        List<LocalDate> runDates = ruleEngine.generateDates(rule, fromDate, toDate);
        Map<Integer, String> days = new HashMap<>();
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            LocalDate currentDate = yearMonth.atDay(i);
            if (runDates.contains(currentDate)) {
                days.put(i, "run");
            } else {
                days.put(i, "no-run");
            }
        }

        return new ScheduleCalendarDto(yearMonth, days);
    }

    @Transactional(readOnly = true)
    public List<ScheduleOverrideDto> getScheduleOverrides(UUID scheduleId) {
        return overrideRepository.findByScheduleId(scheduleId).stream()
                .map(override -> new ScheduleOverrideDto(override.getOverrideDate(), override.getAction().name()))
                .collect(Collectors.toList());
    }
}
