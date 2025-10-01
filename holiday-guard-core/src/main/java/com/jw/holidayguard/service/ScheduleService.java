package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.CreateScheduleRequest;
import com.jw.holidayguard.dto.request.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import com.jw.holidayguard.dto.ScheduleMonthDto;
import com.jw.holidayguard.dto.DeviationDto;
import com.jw.holidayguard.repository.DeviationRepository;
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
    private final RuleRepository ruleRepository;
    private final VersionRepository versionRepository;
    private final DeviationRepository deviationRepository;
    private final RuleEngine ruleEngine;
    private final CurrentUserService currentUserService;

    public ScheduleService(ScheduleRepository repository, RuleRepository ruleRepository, VersionRepository versionRepository, DeviationRepository deviationRepository, RuleEngine ruleEngine, CurrentUserService currentUserService) {
        this.repository = repository;
        this.ruleRepository = ruleRepository;
        this.versionRepository = versionRepository;
        this.deviationRepository = deviationRepository;
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

        Version version = Version.builder()
                .scheduleId(savedSchedule.getId())
                .active(true)
                .build();

        Version savedVersion = versionRepository.save(version);

        Rule rule = Rule.builder()
                .scheduleId(savedSchedule.getId())
                .versionId(savedVersion.getId())
                .ruleType(Rule.RuleType.valueOf(request.getRuleType()))
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
            existing.setActive(updateData.getActive());
        }

        if (updateData.getRuleType() != null) {

            Optional<Rule> latestRuleOpt = findLatestRuleForSchedule(id);

            boolean ruleChanged;
            if (latestRuleOpt.isEmpty()) {
                // CASE 1: No previous rule. Any new rule is a change.
                ruleChanged = true;
            } else {
                // CASE 2: A rule exists. Compare its properties to the new ones.
                Rule latestRule = latestRuleOpt.get();
                Rule.RuleType newRuleType = Rule.RuleType.valueOf(updateData.getRuleType());

                // Use Objects.equals for null-safe comparison
                boolean ruleTypeChanged = !java.util.Objects.equals(latestRule.getRuleType(), newRuleType);
                boolean ruleConfigChanged = !java.util.Objects.equals(latestRule.getRuleConfig(), updateData.getRuleConfig());

                ruleChanged = ruleTypeChanged || ruleConfigChanged;
            }

            if (ruleChanged) {

                // Deactivate old version
                versionRepository
                        .findByScheduleIdAndActiveTrue(id)
                        .ifPresent(v -> v.setActive(false));

                // create & save new version
                Version version = Version.builder()
                        .scheduleId(id)
                        .active(true)
                        .build();
                Version savedVersion = versionRepository.save(version);

                // create & save new rule (on this new version)
                Rule rule = Rule.builder()
                        .scheduleId(id)
                        .versionId(savedVersion.getId())
                        .ruleType(Rule.RuleType.valueOf(updateData.getRuleType()))
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
    public Optional<Rule> findLatestRuleForSchedule(UUID scheduleId) {
        return ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId);
    }

    @Transactional(readOnly = true)
    public ScheduleMonthDto getScheduleCalendar(UUID scheduleId, YearMonth yearMonth) {
        Schedule schedule = findScheduleById(scheduleId);
        Rule rule = findLatestRuleForSchedule(scheduleId)
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

        return new ScheduleMonthDto(yearMonth, days);
    }

    @Transactional(readOnly = true)
    public List<DeviationDto> getScheduleDeviations(UUID scheduleId) {
        return deviationRepository.findByScheduleId(scheduleId).stream()
                .map(override -> new DeviationDto(override.getOverrideDate(), override.getAction().name()))
                .collect(Collectors.toList());
    }
}
