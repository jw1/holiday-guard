package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleRules;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.CreateScheduleRequest;
import com.jw.holidayguard.dto.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleRulesRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository repository;
    private final ScheduleRulesRepository rulesRepository;
    private final ScheduleVersionRepository versionRepository;

    public ScheduleService(ScheduleRepository repository, ScheduleRulesRepository rulesRepository, ScheduleVersionRepository versionRepository) {
        this.repository = repository;
        this.rulesRepository = rulesRepository;
        this.versionRepository = versionRepository;
    }

    public Schedule createSchedule(CreateScheduleRequest request) {
        // Check for duplicate name
        if (repository.findByName(request.getName()).isPresent()) {
            throw new DuplicateScheduleException(request.getName());
        }

        Schedule schedule = Schedule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry())
                .active(request.isActive())
                .build();
        
        Schedule savedSchedule = repository.save(schedule);

        ScheduleVersion version = ScheduleVersion.builder()
                .scheduleId(savedSchedule.getId())
                .active(true)
                .build();
        
        ScheduleVersion savedVersion = versionRepository.save(version);

        ScheduleRules rule = ScheduleRules.builder()
                .scheduleId(savedSchedule.getId())
                .versionId(savedVersion.getId())
                .ruleType(ScheduleRules.RuleType.valueOf(request.getRuleType()))
                .ruleConfig(request.getRuleConfig())
                .build();

        rulesRepository.save(rule);
        
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
            Optional<ScheduleRules> latestRuleOpt = findLatestRuleForSchedule(id);
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
                ScheduleRules rule = ScheduleRules.builder()
                    .scheduleId(id)
                    .versionId(savedVersion.getId())
                    .ruleType(ScheduleRules.RuleType.valueOf(updateData.getRuleType()))
                    .ruleConfig(updateData.getRuleConfig())
                    .build();
                rulesRepository.save(rule);
            }
        }
        
        // JPA automatically detects changes and updates on transaction commit
        return existing;
    }


    @Transactional(readOnly = true)
    public Optional<ScheduleRules> findLatestRuleForSchedule(UUID scheduleId) {
        return rulesRepository.findFirstByScheduleIdOrderByCreatedAtDesc(scheduleId);
    }
}
