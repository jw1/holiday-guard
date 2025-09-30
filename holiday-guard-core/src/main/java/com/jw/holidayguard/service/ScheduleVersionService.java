package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ScheduleVersionService {

    private final ScheduleRepository scheduleRepository;
    private final VersionRepository versionRepository;
    private final RuleRepository ruleRepository;

    public ScheduleVersionService(
            ScheduleRepository scheduleRepository,
            VersionRepository versionRepository,
            RuleRepository ruleRepository) {
        this.scheduleRepository = scheduleRepository;
        this.versionRepository = versionRepository;
        this.ruleRepository = ruleRepository;
    }

    public Version updateScheduleRule(UUID scheduleId, UpdateRuleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // Deactivate current version
        Optional<Version> currentVersion = versionRepository.findByScheduleIdAndActiveTrue(scheduleId);
        if (currentVersion.isPresent()) {
            Version current = currentVersion.get();
            current.setActive(false);
            versionRepository.save(current);
        }

        // Create new version
        Version newVersion = Version.builder()
                .scheduleId(scheduleId)
                .effectiveFrom(request.getEffectiveFrom() != null ? request.getEffectiveFrom() : Instant.now())
                .active(true)
                .build();

        newVersion = versionRepository.save(newVersion);

        // Create rule for new version
        CreateRuleRequest ruleRequest = request.getRule();
        Rule rule = Rule.builder()
                .scheduleId(scheduleId)
                .versionId(newVersion.getId())
                .ruleType(ruleRequest.getRuleType())
                .ruleConfig(ruleRequest.getRuleConfig())
                .effectiveFrom(ruleRequest.getEffectiveFrom())
                .active(ruleRequest.isActive())
                .build();

        ruleRepository.save(rule);

        return newVersion;
    }
}
