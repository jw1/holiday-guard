package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.CreateDeviationRequest;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ScheduleVersionService {

    private final ScheduleRepository scheduleRepository;
    private final VersionRepository versionRepository;
    private final RuleRepository ruleRepository;
    private final DeviationRepository deviationRepository;

    public ScheduleVersionService(
            ScheduleRepository scheduleRepository,
            VersionRepository versionRepository,
            RuleRepository ruleRepository,
            DeviationRepository deviationRepository) {
        this.scheduleRepository = scheduleRepository;
        this.versionRepository = versionRepository;
        this.ruleRepository = ruleRepository;
        this.deviationRepository = deviationRepository;
    }

    public Version updateScheduleRule(Long scheduleId, UpdateRuleRequest request) {
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

        if (null != request.getDeviations()) {
            log.info("Deviations size:  " + request.getDeviations().size());
            // log each deviation
            request.getDeviations().forEach(d -> log.info("asdf " + d));
        } else {
            log.warn("devi was null");
        }

        // Save deviations for new version (if any)
        if (request.getDeviations() != null && !request.getDeviations().isEmpty()) {
            for (CreateDeviationRequest deviationRequest : request.getDeviations()) {
                Deviation deviation = Deviation.builder()
                        .scheduleId(scheduleId)
                        .versionId(newVersion.getId())
                        .deviationDate(deviationRequest.getDeviationDate())
                        .action(deviationRequest.getAction())
                        .reason(deviationRequest.getReason())
                        .createdBy(deviationRequest.getCreatedBy())
                        .expiresAt(deviationRequest.getExpiresAt())
                        .build();

                deviationRepository.save(deviation);
            }
        }

        return newVersion;
    }
}
