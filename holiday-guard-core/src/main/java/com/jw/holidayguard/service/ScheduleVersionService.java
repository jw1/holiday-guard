package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleRules;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.CreateScheduleRuleRequest;
import com.jw.holidayguard.dto.UpdateScheduleRulesRequest;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleRulesRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ScheduleVersionService {
    
    private final ScheduleRepository scheduleRepository;
    private final ScheduleVersionRepository scheduleVersionRepository;
    private final ScheduleRulesRepository scheduleRulesRepository;

    public ScheduleVersionService(
            ScheduleRepository scheduleRepository,
            ScheduleVersionRepository scheduleVersionRepository, 
            ScheduleRulesRepository scheduleRulesRepository) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleVersionRepository = scheduleVersionRepository;
        this.scheduleRulesRepository = scheduleRulesRepository;
    }

    public ScheduleVersion updateScheduleRules(UUID scheduleId, UpdateScheduleRulesRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));
        
        // Deactivate current version
        Optional<ScheduleVersion> currentVersion = scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId);
        if (currentVersion.isPresent()) {
            ScheduleVersion current = currentVersion.get();
            current.setActive(false);
            scheduleVersionRepository.save(current);
        }
        
        // Create new version
        ScheduleVersion newVersion = ScheduleVersion.builder()
            .scheduleId(scheduleId)
            .effectiveFrom(request.getEffectiveFrom() != null ? request.getEffectiveFrom() : Instant.now())
            .active(true)
            .build();
        
        newVersion = scheduleVersionRepository.save(newVersion);
        
        // Create rules for new version
        for (CreateScheduleRuleRequest ruleRequest : request.getRules()) {
            ScheduleRules rule = ScheduleRules.builder()
                .scheduleId(scheduleId)
                .versionId(newVersion.getId())
                .ruleType(ruleRequest.getRuleType())
                .ruleConfig(ruleRequest.getRuleConfig())
                .effectiveFrom(ruleRequest.getEffectiveFrom())
                .active(ruleRequest.isActive())
                .build();
            
            scheduleRulesRepository.save(rule);
        }
        
        return newVersion;
    }
}