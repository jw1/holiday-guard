package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.repository.DeviationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RED: Write failing tests for OverrideApplicator
 * Testing SKIP and FORCE_RUN override logic
 */
@ExtendWith(MockitoExtension.class)
class OverrideApplicatorTest {

    @Mock
    private DeviationRepository deviationRepository;

    @InjectMocks
    private OverrideApplicatorImpl overrideApplicator;
    
    private Long scheduleId;
    private Long versionId;
    private LocalDate fromDate;
    private LocalDate toDate;
    
    @BeforeEach
    void setUp() {
        scheduleId = 1L;
        versionId = 10L;
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
        
        when(deviationRepository.findByScheduleId(scheduleId))
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
        
        Deviation skipOverride = Deviation.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 3))
            .action(Deviation.Action.SKIP)
            .reason("Holiday skip")
            .build();
        when(deviationRepository.findByScheduleId(scheduleId))
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
        
        Deviation forceRunOverride = Deviation.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 3)) // Not in rule dates
            .action(Deviation.Action.FORCE_RUN)
            .reason("Emergency run")
            .build();
        
        when(deviationRepository.findByScheduleId(scheduleId))
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
        
        Deviation skipOverride = Deviation.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 2))
            .action(Deviation.Action.SKIP)
            .reason("Holiday")
            .build();
            
        Deviation forceRunOverride = Deviation.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 6)) // New date
            .action(Deviation.Action.FORCE_RUN)
            .reason("Emergency")
            .build();
        
        when(deviationRepository.findByScheduleId(scheduleId))
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
        
        Deviation skipOverride = Deviation.builder()
            .scheduleId(scheduleId)
            .versionId(versionId)
            .overrideDate(LocalDate.of(2025, 1, 3)) // Not in rule dates
            .action(Deviation.Action.SKIP)
            .reason("Holiday")
            .build();
        
        when(deviationRepository.findByScheduleId(scheduleId))
            .thenReturn(List.of(skipOverride));
        
        List<LocalDate> result = overrideApplicator.applyOverrides(scheduleId, versionId, ruleDates, fromDate, toDate);
        
        assertEquals(2, result.size());
        assertEquals(ruleDates, result); // Should be unchanged
    }
}