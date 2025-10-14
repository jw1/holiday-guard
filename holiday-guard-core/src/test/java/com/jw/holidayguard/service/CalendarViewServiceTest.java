package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.*;
import com.jw.holidayguard.dto.view.DayStatusView;
import com.jw.holidayguard.dto.view.MultiScheduleCalendarView;
import com.jw.holidayguard.dto.view.ScheduleMonthView;
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
        // given - A single schedule with weekdays-only rule for January 2025
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

        // when - Generating calendar for January 2025
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // then - Should return 1 schedule with 31 days
        assertNotNull(result);
        assertEquals(yearMonth, result.yearMonth());
        assertThat(result.schedules()).hasSize(1);

        ScheduleMonthView scheduleView = result.schedules().get(0);
        assertEquals(scheduleId, scheduleView.scheduleId());
        assertEquals("Payroll Schedule", scheduleView.scheduleName());
        assertThat(scheduleView.days()).hasSize(31);

        // Verify specific days
        DayStatusView firstDay = scheduleView.days().stream()
                .filter(d -> d.date().equals(LocalDate.of(2025, 1, 1)))
                .findFirst()
                .orElseThrow();
        // Jan 1, 2025 is Wednesday (weekday) - should run
        assertEquals(RUN, firstDay.status());
    }

    @Test
    void shouldGenerateCalendarForMultipleSchedules() {
        // given - Two different schedules
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

        // when - Generating calendar for both schedules
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId, schedule2Id), yearMonth, true);

        // then - Should return 2 schedules, each with 31 days
        assertNotNull(result);
        assertThat(result.schedules()).hasSize(2);

        // Verify we have entries for both schedules with correct day counts
        ScheduleMonthView schedule1View = result.schedules().stream()
                .filter(s -> s.scheduleId().equals(scheduleId))
                .findFirst()
                .orElseThrow();
        ScheduleMonthView schedule2View = result.schedules().stream()
                .filter(s -> s.scheduleId().equals(schedule2Id))
                .findFirst()
                .orElseThrow();

        assertEquals(31, schedule1View.days().size());
        assertEquals(31, schedule2View.days().size());
    }

    @Test
    void shouldApplySkipDeviation() {

        // given - Schedule with SKIP deviation on Jan 6 (Monday)
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

        // when - Generating calendar
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // then - Jan 6 should be SKIP (deviation overrides rule)
        ScheduleMonthView scheduleView = result.schedules().get(0);
        DayStatusView skipDay = scheduleView.days().stream()
                .filter(d -> d.date().equals(skipDate))
                .findFirst()
                .orElseThrow();

        assertEquals(FORCE_SKIP, skipDay.status());
        assertEquals("Holiday - MLK Day", skipDay.reason());
    }

    @Test
    void shouldApplyForceRunDeviation() {

        // given - Schedule with FORCE_RUN deviation on Jan 4 (Saturday)
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

        // when - Generating calendar
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // then - Jan 4 (Saturday) should be FORCE_RUN (deviation overrides rule)
        ScheduleMonthView scheduleView = result.schedules().get(0);
        DayStatusView forceRunDay = scheduleView.days().stream()
                .filter(d -> d.date().equals(forceRunDate))
                .findFirst()
                .orElseThrow();

        assertEquals(FORCE_RUN, forceRunDay.status());
        assertEquals("Emergency processing", forceRunDay.reason());
    }

    @Test
    void shouldSkipScheduleWithNoRule() {
        // given - Schedule exists but has no rule
        YearMonth yearMonth = YearMonth.of(2025, 1);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.empty());

        // when - Generating calendar
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // then - Should return empty calendar (schedule skipped)
        assertNotNull(result);
        assertThat(result.schedules()).isEmpty();
    }

    @Test
    void shouldSkipScheduleWithNoVersion() {
        // given - Schedule exists but has no active version
        YearMonth yearMonth = YearMonth.of(2025, 1);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.empty());

        // when - Generating calendar
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // then - Should return empty calendar (schedule skipped)
        assertNotNull(result);
        assertThat(result.schedules()).isEmpty();
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
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, false);

        // then Monday, Jan 6 should follow base rule (run on Monday), not SKIP
        ScheduleMonthView scheduleView = result.schedules().get(0);
        DayStatusView jan6 = scheduleView.days()
                .stream()
                .filter(d -> d.date().equals(skipDate))
                .findFirst()
                .orElseThrow();

        assertEquals(RUN, jan6.status());
        assertNull(jan6.reason()); // No deviation reason
    }

    @Test
    void shouldGenerateFullMonthOfDays() {
        // given - A schedule for February 2025 (28 days)
        YearMonth yearMonth = YearMonth.of(2025, 2);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(testVersion));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(testRule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of());

        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenReturn(true); // All days run

        // when - Generating calendar
        MultiScheduleCalendarView result = service.getMultiScheduleCalendar(
                List.of(scheduleId), yearMonth, true);

        // then - Should have exactly 28 days
        assertNotNull(result);
        ScheduleMonthView scheduleView = result.schedules().get(0);
        assertThat(scheduleView.days()).hasSize(28);

        // Verify all dates are present
        List<LocalDate> dates = scheduleView.days().stream()
                .map(DayStatusView::date)
                .toList();

        for (int day = 1; day <= 28; day++) {
            LocalDate expectedDate = LocalDate.of(2025, 2, day);
            assertThat(dates).contains(expectedDate);
        }
    }
}
