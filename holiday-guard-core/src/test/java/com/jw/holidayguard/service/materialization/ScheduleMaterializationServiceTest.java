package com.jw.holidayguard.service.materialization;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.repository.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleMaterializationServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleVersionRepository scheduleVersionRepository;

    @Mock
    private ScheduleRuleRepository scheduleRuleRepository;

    @Mock
    private ScheduleMaterializedCalendarRepository materializedCalendarRepository;

    @Mock
    private RuleEngine ruleEngine;

    @Mock
    private OverrideApplicator overrideApplicator;

    @InjectMocks
    private ScheduleMaterializationService service;

    private UUID scheduleId;
    private UUID versionId;
    private Schedule testSchedule;
    private ScheduleVersion testVersion;
    private ScheduleRule testRule;

    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();
        versionId = UUID.randomUUID();

        testSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .active(true)
                .build();

        testVersion = ScheduleVersion.builder()
                .id(versionId)
                .scheduleId(scheduleId)
                .active(true)
                .build();

        testRule = ScheduleRule.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .ruleType(ScheduleRule.RuleType.WEEKDAYS_ONLY)
                .active(true)
                .build();
    }

    @Test
    void shouldMaterializeCalendarFromRules() {
        // given
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 7);

        List<LocalDate> ruleDates = List.of(
                LocalDate.of(2025, 1, 1), // Wed
                LocalDate.of(2025, 1, 2), // Thu
                LocalDate.of(2025, 1, 3)  // Fri
        );

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId)).thenReturn(Optional.of(testVersion));
        when(scheduleRuleRepository.findByScheduleIdAndVersionIdAndActiveTrue(scheduleId, versionId)).thenReturn(Optional.of(testRule));
        when(ruleEngine.generateDates(testRule, fromDate, toDate)).thenReturn(ruleDates);
        when(overrideApplicator.applyOverrides(eq(scheduleId), eq(versionId), eq(ruleDates), eq(fromDate), eq(toDate))).thenReturn(ruleDates);

        // when
        List<LocalDate> result = service.materializeCalendar(scheduleId, fromDate, toDate);

        // then
        assertEquals(3, result.size());
        assertTrue(result.containsAll(ruleDates));

        verify(materializedCalendarRepository).deleteByScheduleIdAndVersionIdAndOccursOnBetween(scheduleId, versionId, fromDate, toDate);
        verify(materializedCalendarRepository, times(3)).save(any(ScheduleMaterializedCalendar.class));
    }

    @Test
    void shouldThrowExceptionWhenScheduleNotFound() {
        // given
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 7);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            service.materializeCalendar(scheduleId, fromDate, toDate);
        });
    }

    @Test
    void shouldThrowExceptionWhenActiveVersionNotFound() {
        // given
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 7);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            service.materializeCalendar(scheduleId, fromDate, toDate);
        });
    }

    @Test
    void shouldRematerializeExistingSchedule() {
        // given
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate = LocalDate.of(2025, 1, 7);

        List<LocalDate> ruleDates = List.of(LocalDate.of(2025, 1, 2));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleVersionRepository.findByScheduleIdAndActiveTrue(scheduleId)).thenReturn(Optional.of(testVersion));
        when(scheduleRuleRepository.findByScheduleIdAndVersionIdAndActiveTrue(scheduleId, versionId)).thenReturn(Optional.of(testRule));
        when(ruleEngine.generateDates(testRule, fromDate, toDate)).thenReturn(ruleDates);
        when(overrideApplicator.applyOverrides(eq(scheduleId), eq(versionId), eq(ruleDates), eq(fromDate), eq(toDate))).thenReturn(ruleDates);

        // when
        service.materializeCalendar(scheduleId, fromDate, toDate);

        // then
        verify(materializedCalendarRepository).deleteByScheduleIdAndVersionIdAndOccursOnBetween(scheduleId, versionId, fromDate, toDate);
        verify(materializedCalendarRepository, times(1)).save(any(ScheduleMaterializedCalendar.class));
    }
}
