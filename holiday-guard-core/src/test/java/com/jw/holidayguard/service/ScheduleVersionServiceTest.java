package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleRules;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.CreateScheduleRuleRequest;
import com.jw.holidayguard.dto.UpdateScheduleRulesRequest;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
import com.jw.holidayguard.repository.ScheduleRulesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScheduleVersionServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    
    @Mock
    private ScheduleVersionRepository scheduleVersionRepository;
    
    @Mock
    private ScheduleRulesRepository scheduleRulesRepository;

    private ScheduleVersionService scheduleVersionService;
    
    private Schedule testSchedule;
    private UUID scheduleId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduleVersionService = new ScheduleVersionService(
            scheduleRepository, 
            scheduleVersionRepository, 
            scheduleRulesRepository
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
    void shouldCreateNewVersionWhenUpdatingScheduleRules() {
        // Given: A schedule with existing rules
        ScheduleVersion currentVersion = ScheduleVersion.builder()
            .id(UUID.randomUUID())
            .scheduleId(scheduleId)
            .effectiveFrom(Instant.now().minusSeconds(3600))
            .active(true)
            .build();
        
        UpdateScheduleRulesRequest request = new UpdateScheduleRulesRequest();
        request.setEffectiveFrom(Instant.now());
        request.setRules(List.of(
            new CreateScheduleRuleRequest(
                ScheduleRules.RuleType.WEEKDAYS_ONLY, 
                null, 
                LocalDate.now(), 
                true
            )
        ));
        
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(currentVersion));
        when(scheduleVersionRepository.save(any(ScheduleVersion.class)))
            .thenAnswer(invocation -> {
                ScheduleVersion version = invocation.getArgument(0);
                if (version.getId() == null) {
                    version.setId(UUID.randomUUID());
                }
                return version;
            });

        // When: Updating schedule rules
        ScheduleVersion newVersion = scheduleVersionService.updateScheduleRules(scheduleId, request);

        // Then: New version should be created and old version deactivated
        assertNotNull(newVersion);
        verify(scheduleVersionRepository).save(argThat(version -> 
            version.getScheduleId().equals(scheduleId) && version.isActive()
        ));
        verify(scheduleVersionRepository).save(argThat(version -> 
            version.getId().equals(currentVersion.getId()) && !version.isActive()
        ));
        verify(scheduleRulesRepository).save(any(ScheduleRules.class));
    }
}