package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.ScheduleOverride;
import com.jw.holidayguard.repository.ScheduleOverrideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RED: Write failing tests for OverrideApplicator
 * Testing SKIP and FORCE_RUN override logic
 */
@ExtendWith(MockitoExtension.class)
class OverrideApplicatorTest {

    @Mock
    private ScheduleOverrideRepository scheduleOverrideRepository;

    @InjectMocks
    private OverrideApplicatorImpl overrideApplicator;
    
    private UUID scheduleId;
    private UUID versionId;
    private LocalDate fromDate;
    private LocalDate toDate;
    
    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();
        versionId = UUID.randomUUID();
        fromDate = LocalDate.of(2025, 1, 1);
        toDate = LocalDate.of(2025, 1, 7);
    }
    
    @Test
    void shouldReturnRuleDatesWhenNoOverrides() {
        // RED: Test case with no overrides - should return rule dates unchanged
        List<LocalDate> ruleDates = List.of(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 3),
            LocalDate.of(2025, 1, 5)
        );
        
        when(scheduleOverrideRepository.findByScheduleId(scheduleId))
            .thenReturn(List.of());
        
        List<LocalDate> result = overrideApplicator.applyOverrides(scheduleId, versionId, ruleDates, fromDate, toDate);
        
        assertEquals(3, result.size());
        assertEquals(ruleDates, result);
    }
    
    @Test
    void shouldApplySkipOverride() {
        // RED: Test SKIP override - should remove date from rule dates
        List<LocalDate> ruleDates = List.of(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 3), // This will be skipped
            LocalDate.of(2025, 1, 5)
        );
        
        ScheduleOverride skipOverride = ScheduleOverride.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 3))
            .action(ScheduleOverride.OverrideAction.SKIP)
            .reason("Holiday skip")
            .build();
        when(scheduleOverrideRepository.findByScheduleId(scheduleId))
            .thenReturn(List.of(skipOverride));
        
        List<LocalDate> result = overrideApplicator.applyOverrides(scheduleId, versionId, ruleDates, fromDate, toDate);
        
        assertEquals(2, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 1)));
        assertFalse(result.contains(LocalDate.of(2025, 1, 3))); // Should be removed
        assertTrue(result.contains(LocalDate.of(2025, 1, 5)));
    }
    
    @Test
    void shouldApplyForceRunOverride() {
        // RED: Test FORCE_RUN override - should add date even if not in rule dates
        List<LocalDate> ruleDates = List.of(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 5)
        );
        
        ScheduleOverride forceRunOverride = ScheduleOverride.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 3)) // Not in rule dates
            .action(ScheduleOverride.OverrideAction.FORCE_RUN)
            .reason("Emergency run")
            .build();
        
        when(scheduleOverrideRepository.findByScheduleId(scheduleId))
            .thenReturn(List.of(forceRunOverride));
        
        List<LocalDate> result = overrideApplicator.applyOverrides(scheduleId, versionId, ruleDates, fromDate, toDate);
        
        assertEquals(3, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 1)));
        assertTrue(result.contains(LocalDate.of(2025, 1, 3))); // Should be added
        assertTrue(result.contains(LocalDate.of(2025, 1, 5)));
        // Should be sorted
        assertEquals(LocalDate.of(2025, 1, 1), result.get(0));
        assertEquals(LocalDate.of(2025, 1, 3), result.get(1));
        assertEquals(LocalDate.of(2025, 1, 5), result.get(2));
    }
    
    @Test
    void shouldHandleMultipleOverrides() {
        // RED: Test multiple overrides of different types
        List<LocalDate> ruleDates = List.of(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 2), // Will be skipped
            LocalDate.of(2025, 1, 5)
        );
        
        ScheduleOverride skipOverride = ScheduleOverride.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 2))
            .action(ScheduleOverride.OverrideAction.SKIP)
            .reason("Holiday")
            .build();
            
        ScheduleOverride forceRunOverride = ScheduleOverride.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 6)) // New date
            .action(ScheduleOverride.OverrideAction.FORCE_RUN)
            .reason("Emergency")
            .build();
        
        when(scheduleOverrideRepository.findByScheduleId(scheduleId))
            .thenReturn(List.of(skipOverride, forceRunOverride));
        
        List<LocalDate> result = overrideApplicator.applyOverrides(scheduleId, versionId, ruleDates, fromDate, toDate);
        
        assertEquals(3, result.size());
        assertTrue(result.contains(LocalDate.of(2025, 1, 1)));
        assertFalse(result.contains(LocalDate.of(2025, 1, 2))); // Skipped
        assertTrue(result.contains(LocalDate.of(2025, 1, 5)));
        assertTrue(result.contains(LocalDate.of(2025, 1, 6))); // Added
    }
    
    @Test
    void shouldHandleOverrideOnNonRuleDate() {
        // RED: Test SKIP override on date not in rule dates - should have no effect
        List<LocalDate> ruleDates = List.of(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 5)
        );
        
        ScheduleOverride skipOverride = ScheduleOverride.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 3)) // Not in rule dates
            .action(ScheduleOverride.OverrideAction.SKIP)
            .reason("Holiday")
            .build();
        
        when(scheduleOverrideRepository.findByScheduleId(scheduleId))
            .thenReturn(List.of(skipOverride));
        
        List<LocalDate> result = overrideApplicator.applyOverrides(scheduleId, versionId, ruleDates, fromDate, toDate);
        
        assertEquals(2, result.size());
        assertEquals(ruleDates, result); // Should be unchanged
    }
}