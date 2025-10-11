package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Calendar;
import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.RunStatus;
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
import com.jw.holidayguard.service.rule.RuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepo;
    private final RuleRepository ruleRepo;
    private final VersionRepository versionRepo;
    private final DeviationRepository deviationRepo;

    private final RuleEngine ruleEngine;
    private final CurrentUserService currentUserService;

    public ScheduleService(ScheduleRepository scheduleRepo, RuleRepository ruleRepo, VersionRepository versionRepo, DeviationRepository deviationRepo, RuleEngine ruleEngine, CurrentUserService currentUserService) {
        this.scheduleRepo = scheduleRepo;
        this.ruleRepo = ruleRepo;
        this.versionRepo = versionRepo;
        this.deviationRepo = deviationRepo;
        this.ruleEngine = ruleEngine;
        this.currentUserService = currentUserService;
    }

    public Schedule createSchedule(CreateScheduleRequest request) {

        // Check for duplicate name
        if (scheduleRepo.findByName(request.getName()).isPresent()) {
            throw new DuplicateScheduleException(request.getName());
        }

        var username = currentUserService.getCurrentUsername();

        var unsavedSchedule = Schedule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry())
                .active(request.isActive())
                .createdBy(username)
                .updatedBy(username)
                .build();

        Schedule schedule = scheduleRepo.save(unsavedSchedule);

        Version version = versionRepo.save(Version
                .builderFrom(schedule)
                .active(true)
                .build()); // Activate initial version

        // Associate rule with version and save
        ruleRepo.save(Rule
                .builderFrom(schedule, request)
                .versionId(version.getId())
                .build());

        return schedule;
    }

    @Transactional(readOnly = true)
    public Schedule findScheduleById(Long id) {
        return scheduleRepo.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Schedule findScheduleByName(String name) {
        return scheduleRepo.findByName(name)
                .orElseThrow(() -> new ScheduleNotFoundException(name));
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllActiveSchedules() {
        return scheduleRepo.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Schedule> findAllSchedules() {
        return scheduleRepo.findAll();
    }

    public Schedule updateSchedule(Long id, UpdateScheduleRequest updateData) {

        // Find existing schedule (throws exception if not found)
        Schedule existing = findScheduleById(id);

        // Check for name conflicts only if name is being changed
        if (updateData.getName() != null && !updateData.getName().equals(existing.getName())) {
            if (scheduleRepo.findByName(updateData.getName()).isPresent()) {
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

                // Get the current active version to use toNewVersion()
                Optional<Version> currentVersionOpt = versionRepo.findByScheduleIdAndActiveTrue(id);

                Rule newRule = Rule.builder()
                        .scheduleId(id)
                        .ruleType(Rule.RuleType.valueOf(updateData.getRuleType()))
                        .ruleConfig(updateData.getRuleConfig())
                        .build();

                // Use domain model factory method to create new version
                Version newVersion;
                if (currentVersionOpt.isPresent()) {
                    Version currentVersion = currentVersionOpt.get();
                    currentVersion.setActive(false); // Deactivate old version
                    newVersion = Version.builderFrom(existing).build();
                } else {
                    newVersion = Version.builderFrom(existing).build();
                }

                newVersion.setActive(true);
                Version savedVersion = versionRepo.save(newVersion);

                // Associate rule with the persisted version
                newRule.setVersionId(savedVersion.getId());
                ruleRepo.save(newRule);
            }
        }

        existing.setUpdatedBy(currentUserService.getCurrentUsername());

        // JPA automatically detects changes and updates on transaction commit
        return existing;
    }


    @Transactional(readOnly = true)
    public Optional<Rule> findLatestRuleForSchedule(Long scheduleId) {
        return ruleRepo.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId);
    }

    @Transactional(readOnly = true)
    public ScheduleMonthDto getScheduleCalendar(Long scheduleId, YearMonth yearMonth) {
        Schedule schedule = findScheduleById(scheduleId);

        // Get active version
        Version activeVersion = versionRepo.findByScheduleIdAndActiveTrue(scheduleId)
                .orElseThrow(() -> new IllegalStateException("Schedule has no active version: " + scheduleId));

        // Fetch rule for active version
        Rule rule = ruleRepo.findByVersionId(activeVersion.getId())
                .orElseThrow(() -> new IllegalStateException("No rule found for version: " + activeVersion.getId()));

        LocalDate fromDate = yearMonth.atDay(1);
        LocalDate toDate = yearMonth.atEndOfMonth();

        log.info("Getting calendar for schedule rule type: {}, config: {}", rule.getRuleType(), rule.getRuleConfig());

        // Fetch deviations for active version
        List<Deviation> deviations = deviationRepo.findByScheduleIdAndVersionId(scheduleId, activeVersion.getId());

        // Create Calendar abstraction - encapsulates shouldRun business logic
        // RuleEngine implements Calendar.RuleEvaluator, so we can use it directly
        Calendar calendar = new Calendar(schedule, rule, deviations, ruleEngine::shouldRun);

        // Query date range using Calendar - guarantees algorithm consistency
        Map<LocalDate, Boolean> shouldRunMap = calendar.shouldRun(fromDate, toDate);

        // Convert to DTO format (Map<Integer, RunStatus>)
        Map<Integer, RunStatus> days = new HashMap<>();
        for (Map.Entry<LocalDate, Boolean> entry : shouldRunMap.entrySet()) {

            LocalDate date = entry.getKey();
            int dayOfMonth = date.getDayOfMonth();
            boolean shouldRun = entry.getValue();

            // Find deviation for this date to determine if it's a forced status
            Optional<Deviation> deviationOpt = deviations.stream()
                    .filter(d -> d.getDeviationDate().equals(date))
                    .findFirst();

            RunStatus status = deviationOpt.map
                    (d -> RunStatus.fromCalendar(shouldRun, d))
                    .orElse(RunStatus.fromCalendar(shouldRun));

            days.put(dayOfMonth, status);
        }

        return new ScheduleMonthDto(yearMonth, days);
    }

    @Transactional(readOnly = true)
    public List<DeviationDto> getScheduleDeviations(Long scheduleId) {
        // Find the active version for this schedule
        Optional<Version> activeVersion = versionRepo.findByScheduleIdAndActiveTrue(scheduleId);

        if (activeVersion.isEmpty()) {
            // No active version means no deviations
            log.warn("no active version found");
            return List.of();
        } else {
            log.info("active version found: {}, schedule id: {}", activeVersion.get().getId(), scheduleId);
        }

        // Query deviations for the active version only
        var deviations = deviationRepo.findByScheduleIdAndVersionId(scheduleId, activeVersion.get().getId()).stream()
                .map(deviation -> new DeviationDto(deviation.getDeviationDate(), deviation.getAction().name(), deviation.getReason()))
                .collect(Collectors.toList());
        log.info("found {} deviations", deviations.size());

        return deviations;
    }
}
