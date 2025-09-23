package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.dto.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.ShouldRunQueryResponse;
import com.jw.holidayguard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    private ScheduleMaterializedCalendarRepository materializedCalendarRepository;
    
    @Mock
    private ScheduleOverrideRepository overrideRepository;
    
    @Mock
    private ScheduleQueryLogRepository queryLogRepository;

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
    void shouldReturnTrueWhenDateExistsInMaterializedCalendar() {
        // Given: A date exists in the materialized calendar (should run = YES)
        LocalDate queryDate = LocalDate.now().plusDays(5); // Future date
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(queryDate, "payroll-service");
        
        ScheduleMaterializedCalendar calendarEntry = ScheduleMaterializedCalendar.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .occursOn(queryDate)
            .status(ScheduleMaterializedCalendar.OccurrenceStatus.SCHEDULED)
            .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(materializedCalendarRepository.findByScheduleIdAndVersionIdAndOccursOn(scheduleId, versionId, queryDate))
            .thenReturn(Optional.of(calendarEntry));
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.Collections.emptyList());
        when(queryLogRepository.save(any(ScheduleQueryLog.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Querying should I run today
        ShouldRunQueryResponse response = service.shouldRunToday(scheduleId, request);

        // Then: Should return true with explanation
        assertNotNull(response);
        assertTrue(response.isShouldRun());
        assertEquals("Scheduled to run - found in materialized calendar", response.getReason());
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
    void shouldReturnFalseWhenDateNotInMaterializedCalendar() {
        // Given: A date does not exist in materialized calendar (should run = NO)
        LocalDate queryDate = LocalDate.now().plusDays(10); // Future date
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(queryDate, "payroll-service");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId))
            .thenReturn(Optional.of(activeVersion));
        when(materializedCalendarRepository.findByScheduleIdAndVersionIdAndOccursOn(scheduleId, versionId, queryDate))
            .thenReturn(Optional.empty());
        when(overrideRepository.findByScheduleId(scheduleId))
            .thenReturn(java.util.Collections.emptyList());
        when(queryLogRepository.save(any(ScheduleQueryLog.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Querying should I run today
        ShouldRunQueryResponse response = service.shouldRunToday(scheduleId, request);

        // Then: Should return false with explanation
        assertNotNull(response);
        assertFalse(response.isShouldRun());
        assertEquals("Not scheduled to run - date not found in materialized calendar", response.getReason());
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