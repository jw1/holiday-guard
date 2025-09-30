package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VersionServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private RuleRepository ruleRepository;

    private ScheduleVersionService scheduleVersionService;

    private Schedule testSchedule;
    private UUID scheduleId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduleVersionService = new ScheduleVersionService(
                scheduleRepository,
                versionRepository,
                ruleRepository
        );

        scheduleId = UUID.randomUUID();
        testSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .description("Test Description")
                .country("US")
                .active(true)
                .build();
    }

    @Test
    void shouldCreateNewVersionWhenUpdatingScheduleRule() {
        // given
        Version currentVersion = Version.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .effectiveFrom(Instant.now().minusSeconds(3600))
                .active(true)
                .build();

        UpdateRuleRequest request = new UpdateRuleRequest();
        request.setEffectiveFrom(Instant.now());
        request.setRule(
                new CreateRuleRequest(
                        Rule.RuleType.WEEKDAYS_ONLY,
                        null,
                        LocalDate.now(),
                        true
                )
        );

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(currentVersion));
        when(versionRepository.save(any(Version.class)))
                .thenAnswer(invocation -> {
                    Version version = invocation.getArgument(0);
                    if (version.getId() == null) {
                        version.setId(UUID.randomUUID());
                    }
                    return version;
                });

        // when
        Version newVersion = scheduleVersionService.updateScheduleRule(scheduleId, request);

        // then
        assertNotNull(newVersion);
        verify(versionRepository).save(argThat(version ->
                version.getScheduleId().equals(scheduleId) && version.isActive()
        ));
        verify(versionRepository).save(argThat(version ->
                version.getId().equals(currentVersion.getId()) && !version.isActive()
        ));
        verify(ruleRepository).save(any(Rule.class));
    }
}
