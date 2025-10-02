package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.QueryLog;
import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.response.ShouldRunQueryResponse;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.QueryLogRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.service.materialization.RuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleQueryServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    
    @Mock
    private VersionRepository versionRepository;
    
    @Mock
    private DeviationRepository overrideRepository;
    
    @Mock
    private QueryLogRepository queryLogRepository;

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RuleEngine ruleEngine;

    @InjectMocks
    private ScheduleQueryService service;
    
    private Schedule testSchedule;
    private Version activeVersion;
    private Long scheduleId;
    private Long versionId;

    @BeforeEach
    void setUp() {
        scheduleId = 1L;
        versionId = 10L;

        testSchedule = Schedule.builder()
            .id(scheduleId)
            .name("Payroll Schedule")
            .active(true)
            .build();

        activeVersion = Version.builder()
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
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(ruleRepository.findByVersionId(versionId))
            .thenReturn(Optional.of(new Rule()));
        when(ruleEngine.shouldRun(any(Rule.class), eq(queryDate))).thenReturn(true);
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.Collections.emptyList());
        when(queryLogRepository.save(any(QueryLog.class)))
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
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(ruleRepository.findByVersionId(versionId))
            .thenReturn(Optional.of(new Rule()));
        when(ruleEngine.shouldRun(any(Rule.class), eq(queryDate))).thenReturn(false);
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.Collections.emptyList());
        when(queryLogRepository.save(any(QueryLog.class)))
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
            
        Deviation skipOverride = Deviation.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(queryDate)
            .action(Deviation.Action.SKIP)
            .reason("Independence Day - holiday skip")
            .build();

        when(scheduleRepository.findById(scheduleId))
            .thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.List.of(skipOverride));
        when(queryLogRepository.save(any(QueryLog.class)))
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
            log.isDeviationApplied() && !log.isShouldRunResult()
        ));
    }
}