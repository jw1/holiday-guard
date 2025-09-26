package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleQueryLog;
import com.jw.holidayguard.domain.ScheduleOverride;
import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.ShouldRunQueryResponse;
import com.jw.holidayguard.repository.ScheduleOverrideRepository;
import com.jw.holidayguard.repository.ScheduleQueryLogRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
import com.jw.holidayguard.repository.ScheduleRuleRepository;
import com.jw.holidayguard.service.materialization.RuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleQueryServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    
    @Mock
    private ScheduleVersionRepository scheduleVersionRepository;
    
    @Mock
    private ScheduleOverrideRepository overrideRepository;
    
    @Mock
    private ScheduleQueryLogRepository queryLogRepository;

    @Mock
    private ScheduleRuleRepository scheduleRuleRepository;

    @Mock
    private RuleEngine ruleEngine;

    @InjectMocks
    private ScheduleQueryService service;
    
    private Schedule testSchedule;
    private ScheduleVersion activeVersion;
    private UUID scheduleId;
    private UUID versionId;

    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();
        versionId = UUID.randomUUID();
        
        testSchedule = Schedule.builder()
            .id(scheduleId)
            .name("Payroll Schedule")
            .active(true)
            .build();
            
        activeVersion = ScheduleVersion.builder()
            .id(versionId)
            .scheduleId(scheduleId)
            .active(true)
            .build();
    }

    @Test
    void shouldReturnTrueWhenAllRulesMatch() {
        // Given: All rules match for the given date
        LocalDate queryDate = LocalDate.now().plusDays(5);
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(queryDate, "payroll-service");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(scheduleRuleRepository.findByVersionId(versionId))
            .thenReturn(Optional.of(new ScheduleRule()));
        when(ruleEngine.shouldRun(any(ScheduleRule.class), eq(queryDate))).thenReturn(true);
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.Collections.emptyList());
        when(queryLogRepository.save(any(ScheduleQueryLog.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Querying should I run today
        ShouldRunQueryResponse response = service.shouldRunToday(scheduleId, request);

        // Then: Should return true with explanation
        assertNotNull(response);
        assertTrue(response.isShouldRun());
        assertEquals("Scheduled to run - rule matches", response.getReason());
        assertFalse(response.isOverrideApplied());
        assertEquals(versionId, response.getVersionId());
        
        // Should log the query
        verify(queryLogRepository).save(argThat(log -> 
            log.getScheduleId().equals(scheduleId) &&
            log.getQueryDate().equals(queryDate) &&
            log.isShouldRunResult() &&
            log.getClientIdentifier().equals("payroll-service")
        ));
    }

    @Test
    void shouldReturnFalseWhenAnyRuleFails() {
        // Given: At least one rule fails for the given date
        LocalDate queryDate = LocalDate.now().plusDays(10);
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(queryDate, "payroll-service");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(scheduleRuleRepository.findByVersionId(versionId))
            .thenReturn(Optional.of(new ScheduleRule()));
        when(ruleEngine.shouldRun(any(ScheduleRule.class), eq(queryDate))).thenReturn(false);
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.Collections.emptyList());
        when(queryLogRepository.save(any(ScheduleQueryLog.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Querying should I run today
        ShouldRunQueryResponse response = service.shouldRunToday(scheduleId, request);

        // Then: Should return false with explanation
        assertNotNull(response);
        assertFalse(response.isShouldRun());
        assertEquals("Not scheduled to run - rule does not match", response.getReason());
        assertFalse(response.isOverrideApplied());
        
        // Should log the query
        verify(queryLogRepository).save(argThat(log -> 
            log.getScheduleId().equals(scheduleId) &&
            log.getQueryDate().equals(queryDate) &&
            !log.isShouldRunResult()
        ));
    }

    @Test
    void shouldApplySkipOverrideWhenPresent() {
        // Given: A date exists in calendar but has a SKIP override
        LocalDate queryDate = LocalDate.now().plusDays(15); // Future date for holiday simulation
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(queryDate, "payroll-service");
            
        ScheduleOverride skipOverride = ScheduleOverride.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(queryDate)
            .action(ScheduleOverride.OverrideAction.SKIP)
            .reason("Independence Day - holiday skip")
            .build();

        when(scheduleRepository.findById(scheduleId))
            .thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.List.of(skipOverride));
        when(queryLogRepository.save(any(ScheduleQueryLog.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Querying should I run today
        ShouldRunQueryResponse response = service.shouldRunToday(scheduleId, request);

        // Then: Should return false due to override
        assertNotNull(response);
        assertFalse(response.isShouldRun());
        assertEquals("Override applied: Independence Day - holiday skip", response.getReason());
        assertTrue(response.isOverrideApplied());
        
        // Should log the query with override flag
        verify(queryLogRepository).save(argThat(log -> 
            log.isOverrideApplied() && !log.isShouldRunResult()
        ));
    }
}