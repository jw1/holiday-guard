package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.dto.CalendarDayDto;
import com.jw.holidayguard.dto.MultiScheduleCalendarDto;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import com.jw.holidayguard.service.rule.RuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static com.jw.holidayguard.domain.RunStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for CalendarViewService using Calendar abstraction.
 * Validates multi-schedule calendar generation with deviations.
 */
@ExtendWith(MockitoExtension.class)
class CalendarViewServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private RuleEngine ruleEngine;

    @InjectMocks
    private CalendarViewService service;

    private Schedule testSchedule;
    private Version testVersion;
    private Rule testRule;
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

        testVersion = Version.builder()
                .id(versionId)
                .scheduleId(scheduleId)
                .active(true)
                .build();

        testRule = Rule.builder()
                .id(100L)
                .scheduleId(scheduleId)
                .versionId(versionId)
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .build();
    }

    @Test
    void shouldGenerateCalendarForSingleSchedule() {
        // Given: A single schedule with weekdays-only rule for January 2025
        YearMonth yearMonth = YearMonth.of(2025, 1);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(testRule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of());

        // Mock RuleEngine to return true for weekdays (Mon-Fri)
        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return dayOfWeek >= 1 && dayOfWeek <= 5; // Mon-Fri
                });

        // When: Generating calendar for January 2025
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // Then: Should return 31 days (full month)
        assertNotNull(result);
        assertEquals(yearMonth, result.getYearMonth());
        assertThat(result.getDays()).hasSize(31);

        // Verify specific days
        CalendarDayDto firstDay = result.getDays().stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2025, 1, 1)))
                .findFirst()
                .orElseThrow();
        assertEquals("Payroll Schedule", firstDay.getScheduleName());
        assertEquals(scheduleId, firstDay.getScheduleId());
        // Jan 1, 2025 is Wednesday (weekday) - should run
        assertEquals(RUN, firstDay.getStatus());
    }

    @Test
    void shouldGenerateCalendarForMultipleSchedules() {
        // Given: Two different schedules
        Long schedule2Id = 2L;
        Long version2Id = 20L;

        Schedule schedule2 = Schedule.builder()
                .id(schedule2Id)
                .name("Report Schedule")
                .active(true)
                .build();

        Version version2 = Version.builder()
                .id(version2Id)
                .scheduleId(schedule2Id)
                .active(true)
                .build();

        Rule rule2 = Rule.builder()
                .id(200L)
                .scheduleId(schedule2Id)
                .versionId(version2Id)
                .ruleType(Rule.RuleType.ALL_DAYS)
                .build();

        YearMonth yearMonth = YearMonth.of(2025, 1);

        // Mock first schedule (weekdays only)
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(testRule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of());

        // Mock second schedule (all days)
        when(scheduleRepository.findById(schedule2Id)).thenReturn(Optional.of(schedule2));
        when(versionRepository.findByScheduleIdAndActiveTrue(schedule2Id))
                .thenReturn(Optional.of(version2));
        when(ruleRepository.findByVersionId(version2Id)).thenReturn(Optional.of(rule2));
        when(deviationRepository.findByScheduleIdAndVersionId(schedule2Id, version2Id))
                .thenReturn(List.of());

        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    Rule rule = invocation.getArgument(0);
                    LocalDate date = invocation.getArgument(1);
                    if (rule.getRuleType() == Rule.RuleType.ALL_DAYS) {
                        return true;
                    } else {
                        int dayOfWeek = date.getDayOfWeek().getValue();
                        return dayOfWeek >= 1 && dayOfWeek <= 5;
                    }
                });

        // When: Generating calendar for both schedules
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId, schedule2Id), yearMonth, true);

        // Then: Should return 62 days (31 days × 2 schedules)
        assertNotNull(result);
        assertThat(result.getDays()).hasSize(62);

        // Verify we have entries for both schedules
        long schedule1Count = result.getDays().stream()
                .filter(d -> d.getScheduleId().equals(scheduleId))
                .count();
        long schedule2Count = result.getDays().stream()
                .filter(d -> d.getScheduleId().equals(schedule2Id))
                .count();

        assertEquals(31, schedule1Count);
        assertEquals(31, schedule2Count);
    }

    @Test
    void shouldApplySkipDeviation() {

        // Given: Schedule with SKIP deviation on Jan 6 (Monday)
        LocalDate skipDate = LocalDate.of(2025, 1, 6);
        YearMonth yearMonth = YearMonth.of(2025, 1);

        Deviation skipDeviation = Deviation.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .deviationDate(skipDate)
                .action(RunStatus.FORCE_SKIP)
                .reason("Holiday - MLK Day")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(testRule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of(skipDeviation));

        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return dayOfWeek >= 1 && dayOfWeek <= 5; // Weekdays
                });

        // When: Generating calendar
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // Then: Jan 6 should be SKIP (deviation overrides rule)
        CalendarDayDto skipDay = result.getDays().stream()
                .filter(d -> d.getDate().equals(skipDate))
                .findFirst()
                .orElseThrow();

        assertEquals(FORCE_SKIP, skipDay.getStatus());
        assertEquals("Holiday - MLK Day", skipDay.getReason());
    }

    @Test
    void shouldApplyForceRunDeviation() {

        // Given: Schedule with FORCE_RUN deviation on Jan 4 (Saturday)
        LocalDate forceRunDate = LocalDate.of(2025, 1, 4);
        YearMonth yearMonth = YearMonth.of(2025, 1);

        Deviation forceRunDeviation = Deviation.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .deviationDate(forceRunDate)
                .action(RunStatus.FORCE_RUN)
                .reason("Emergency processing")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(testRule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of(forceRunDeviation));

        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return dayOfWeek >= 1 && dayOfWeek <= 5; // Weekdays only
                });

        // When: Generating calendar
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // Then: Jan 4 (Saturday) should be FORCE_RUN (deviation overrides rule)
        CalendarDayDto forceRunDay = result.getDays().stream()
                .filter(d -> d.getDate().equals(forceRunDate))
                .findFirst()
                .orElseThrow();

        assertEquals(FORCE_RUN, forceRunDay.getStatus());
        assertEquals("Emergency processing", forceRunDay.getReason());
    }

    @Test
    void shouldSkipScheduleWithNoRule() {
        // Given: Schedule exists but has no rule
        YearMonth yearMonth = YearMonth.of(2025, 1);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.empty());

        // When: Generating calendar
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // Then: Should return empty calendar (schedule skipped)
        assertNotNull(result);
        assertThat(result.getDays()).isEmpty();
    }

    @Test
    void shouldSkipScheduleWithNoVersion() {
        // Given: Schedule exists but has no active version
        YearMonth yearMonth = YearMonth.of(2025, 1);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.empty());

        // When: Generating calendar
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // Then: Should return empty calendar (schedule skipped)
        assertNotNull(result);
        assertThat(result.getDays()).isEmpty();
    }

    @Test
    void shouldHandleDeviationsWhenDisabled() {

        // given: schedule w/ deviation, but includeDeviations=false
        LocalDate skipDate = LocalDate.of(2025, 1, 6); // known "Monday"
        YearMonth yearMonth = YearMonth.of(2025, 1);

        Deviation forceSkip = Deviation.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .deviationDate(skipDate)
                .action(RunStatus.FORCE_SKIP)
                .reason("Holiday")
                .build();

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId))
                .thenReturn(Optional.of(testRule));

        // deviation repo should NOT be called when includeDeviations=false
        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return dayOfWeek >= 1 && dayOfWeek <= 5;
                });

        // when generating calendar with includeDeviations=false
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, false);

        // then Monday, Jan 6 should follow base rule (run on Monday), not SKIP
        CalendarDayDto jan6 = result.getDays()
                .stream()
                .filter(d -> d.getDate().equals(skipDate))
                .findFirst()
                .orElseThrow();

        assertEquals(RUN, jan6.getStatus());
        assertNull(jan6.getReason()); // No deviation reason
    }

    @Test
    void shouldGenerateFullMonthOfDays() {
        // Given: A schedule for February 2025 (28 days)
        YearMonth yearMonth = YearMonth.of(2025, 2);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(testRule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of());

        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenReturn(true); // All days run

        // When: Generating calendar
        MultiScheduleCalendarDto result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // Then: Should have exactly 28 days
        assertNotNull(result);
        assertThat(result.getDays()).hasSize(28);

        // Verify all dates are present
        List<LocalDate> dates = result.getDays().stream()
                .map(CalendarDayDto::getDate)
                .toList();

        for (int day = 1; day <= 28; day++) {
            LocalDate expectedDate = LocalDate.of(2025, 2, day);
            assertThat(dates).contains(expectedDate);
        }
    }
}
