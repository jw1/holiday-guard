package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.CreateDeviationRequest;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import com.jw.holidayguard.dto.response.DeviationResponse;
import com.jw.holidayguard.dto.response.RuleResponse;
import com.jw.holidayguard.dto.response.VersionResponse;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    public VersionResponse updateScheduleRule(Long scheduleId, UpdateRuleRequest request) {
        scheduleRepository.findById(scheduleId)
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

        Rule savedRule = ruleRepository.save(rule);

        // Save deviations for new version (if any)
        List<Deviation> savedDeviations = new ArrayList<>();
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

                savedDeviations.add(deviationRepository.save(deviation));
            }
        }

        return toVersionResponse(newVersion, savedRule, savedDeviations);
    }

    private VersionResponse toVersionResponse(Version version, Rule rule, List<Deviation> deviations) {
        RuleResponse ruleResponse = new RuleResponse(
                rule.getId(),
                rule.getScheduleId(),
                rule.getVersionId(),
                rule.getRuleType(),
                rule.getRuleConfig(),
                rule.getEffectiveFrom(),
                rule.getCreatedAt(),
                rule.isActive()
        );

        List<DeviationResponse> deviationResponses = deviations.stream()
                .map(d -> new DeviationResponse(
                        d.getId(),
                        d.getScheduleId(),
                        d.getVersionId(),
                        d.getDeviationDate(),
                        d.getAction(),
                        d.getReason(),
                        d.getCreatedBy(),
                        d.getCreatedAt(),
                        d.getExpiresAt()
                ))
                .toList();

        return new VersionResponse(
                version.getId(),
                version.getScheduleId(),
                version.getEffectiveFrom(),
                version.getCreatedAt(),
                version.isActive(),
                List.of(ruleResponse),
                deviationResponses
        );
    }
}
