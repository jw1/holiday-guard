package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.RunStatus;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.ScheduleMonthDto;
import com.jw.holidayguard.dto.request.CreateScheduleRequest;
import com.jw.holidayguard.dto.request.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.DeviationRepository;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import com.jw.holidayguard.service.rule.RuleEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository repository;

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private RuleEngine ruleEngine;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ScheduleService service;

    @Test
    void createSchedule() {

        // given - weekdays only schedule, already saved schedule w/ version
        var request = CreateScheduleRequest.builder()
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .ruleType("WEEKDAYS_ONLY")
                .ruleConfig("")
                .build();

        var savedSchedule = Schedule.builder()
                .id(null)
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        var savedVersion = Version.builder()
                .id(null)
                .build();

        when(repository.findByName(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Schedule.class))).thenReturn(savedSchedule);
        when(versionRepository.save(any(Version.class))).thenReturn(savedVersion);
        when(currentUserService.getCurrentUsername()).thenReturn("test-user");

        // when - schedule is created
        service.createSchedule(request);

        // then - repo is called, fields are defaulted, rule & version saved
        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(repository).save(scheduleCaptor.capture());
        
        assertThat(scheduleCaptor.getValue().getCreatedBy()).isEqualTo("test-user");
        assertThat(scheduleCaptor.getValue().getUpdatedBy()).isEqualTo("test-user");
        
        verify(versionRepository).save(any(Version.class));
        verify(ruleRepository).save(any(Rule.class));
    }

    @Test
    void createScheduleWithDuplicateName() {

        // given - schedule with a particular name
        var request = CreateScheduleRequest.builder()
                .name("Existing Schedule")
                .build();

        // when - repo returns a schedule with that name already
        when(repository.findByName("Existing Schedule")).thenReturn(Optional.of(new Schedule()));

        // then - exception thrown when dupe is saved
        assertThatThrownBy(() -> service.createSchedule(request))
                .isInstanceOf(DuplicateScheduleException.class);
    }

    @Test
    void findScheduleById() {

        // given - existing schedule with some ID
        Long scheduleId = null;
        var existingSchedule = Schedule.builder().id(scheduleId).build();
        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));

        // when - it is searched for
        var result = service.findScheduleById(scheduleId);

        // then - it is found
        assertThat(result).isEqualTo(existingSchedule);
    }

    @Test
    void findScheduleByIdNotFound() {

        // given - id not in schedule table
        Long scheduleId = null;
        when(repository.findById(scheduleId)).thenReturn(Optional.empty());

        // when/then - exception thrown because it is not found
        assertThatThrownBy(() -> service.findScheduleById(scheduleId))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    @Test
    void updateSchedule_shouldCreateNewVersion_whenRuleIsAddedForTheFirstTime() {
        // given
        Long scheduleId = null;
        var existingSchedule = new Schedule();
        existingSchedule.setName("Original Name");
        existingSchedule.setCreatedBy("original-user");

        var request = UpdateScheduleRequest.builder()
                .name("Updated Name")
                .ruleType("CRON_EXPRESSION")
                .ruleConfig("* * * * *")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(repository.findByName("Updated Name")).thenReturn(Optional.empty());
        when(ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId)).thenReturn(Optional.empty()); // No existing rule
        when(versionRepository.save(any(Version.class))).thenReturn(new Version());
        when(currentUserService.getCurrentUsername()).thenReturn("test-user");

        // when
        var result = service.updateSchedule(scheduleId, request);

        // then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getUpdatedBy()).isEqualTo("test-user");
        verify(versionRepository, times(1)).save(any(Version.class));
        verify(ruleRepository, times(1)).save(any(Rule.class));
    }

    @Test
    void updateSchedule_shouldCreateNewVersion_whenRuleIsChanged() {
        // given
        Long scheduleId = null;
        var existingSchedule = new Schedule();
        var existingRule = Rule.builder().ruleType(Rule.RuleType.WEEKDAYS_ONLY).ruleConfig("").build();

        var request = UpdateScheduleRequest.builder()
                .ruleType("CRON_EXPRESSION") // Different rule type
                .ruleConfig("* * * * *")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId)).thenReturn(Optional.of(existingRule));
        when(versionRepository.save(any(Version.class))).thenReturn(new Version());

        // when
        service.updateSchedule(scheduleId, request);

        // then
        verify(versionRepository, times(1)).save(any(Version.class));
        verify(ruleRepository, times(1)).save(any(Rule.class));
    }

    @Test
    void updateSchedule_shouldNotCreateNewVersion_whenRuleIsUnchanged() {
        // given
        Long scheduleId = null;
        var existingSchedule = new Schedule();
        var existingRule = Rule.builder().ruleType(Rule.RuleType.WEEKDAYS_ONLY).ruleConfig("").build();

        var request = UpdateScheduleRequest.builder()
                .ruleType("WEEKDAYS_ONLY") // Same rule type and config
                .ruleConfig("")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId)).thenReturn(Optional.of(existingRule));

        // when
        service.updateSchedule(scheduleId, request);

        // then
        verify(versionRepository, never()).save(any(Version.class));
        verify(ruleRepository, never()).save(any(Rule.class));
    }

    @Test
    void getScheduleCalendar_shouldReturnMonthWithRunDays() {
        // Given: Schedule with weekdays-only rule for January 2025
        Long scheduleId = 1L;
        Long versionId = 10L;
        YearMonth yearMonth = YearMonth.of(2025, 1);

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .build();

        Version version = Version.builder()
                .id(versionId)
                .scheduleId(scheduleId)
                .active(true)
                .build();

        Rule rule = Rule.builder()
                .id(100L)
                .scheduleId(scheduleId)
                .versionId(versionId)
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(version));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(rule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of());

        // Mock RuleEngine to return true for weekdays (Mon-Fri)
        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return dayOfWeek >= 1 && dayOfWeek <= 5; // Mon-Fri
                });

        // When: Getting calendar for January 2025
        ScheduleMonthDto result = service.getScheduleCalendar(scheduleId, yearMonth);

        // Then: Should return 31 days with correct run/no-run status
        assertThat(result).isNotNull();
        assertThat(result.getYearMonth()).isEqualTo(yearMonth);
        assertThat(result.getDays()).hasSize(31);

        // Jan 1, 2025 is Wednesday (weekday) - should run
        assertThat(result.getDays().get(1)).isEqualTo(com.jw.holidayguard.domain.RunStatus.RUN);

        // Jan 4, 2025 is Saturday (weekend) - should not run
        assertThat(result.getDays().get(4)).isEqualTo(com.jw.holidayguard.domain.RunStatus.SKIP);

        // Jan 6, 2025 is Monday (weekday) - should run
        assertThat(result.getDays().get(6)).isEqualTo(com.jw.holidayguard.domain.RunStatus.RUN);
    }

    @Test
    void getScheduleCalendar_shouldApplyDeviations() {
        // Given: Schedule with SKIP deviation on a weekday
        Long scheduleId = 1L;
        Long versionId = 10L;
        YearMonth yearMonth = YearMonth.of(2025, 1);
        LocalDate skipDate = LocalDate.of(2025, 1, 6); // Monday

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .build();

        Version version = Version.builder()
                .id(versionId)
                .scheduleId(scheduleId)
                .active(true)
                .build();

        Rule rule = Rule.builder()
                .id(100L)
                .scheduleId(scheduleId)
                .versionId(versionId)
                .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
                .build();

        Deviation skipDeviation = Deviation.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .deviationDate(skipDate)
                .action(RunStatus.FORCE_SKIP)
                .reason("Holiday")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.of(version));
        when(ruleRepository.findByVersionId(versionId)).thenReturn(Optional.of(rule));
        when(deviationRepository.findByScheduleIdAndVersionId(scheduleId, versionId))
                .thenReturn(List.of(skipDeviation));

        // Mock RuleEngine to return true for weekdays
        when(ruleEngine.shouldRun(any(Rule.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return dayOfWeek >= 1 && dayOfWeek <= 5;
                });

        // When: Getting calendar
        ScheduleMonthDto result = service.getScheduleCalendar(scheduleId, yearMonth);

        // Then: Jan 6 (Monday) should be FORCE_SKIP due to SKIP deviation
        assertThat(result.getDays().get(6)).isEqualTo(com.jw.holidayguard.domain.RunStatus.FORCE_SKIP);

        // Jan 7 (Tuesday) should still be RUN (no deviation)
        assertThat(result.getDays().get(7)).isEqualTo(com.jw.holidayguard.domain.RunStatus.RUN);
    }

    @Test
    void getScheduleCalendar_shouldThrowExceptionWhenNoActiveVersion() {
        // Given: Schedule with no active version
        Long scheduleId = 1L;
        YearMonth yearMonth = YearMonth.of(2025, 1);

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionRepository.findByScheduleIdAndActiveTrue(scheduleId))
                .thenReturn(Optional.empty());

        // When/Then: Should throw exception
        assertThatThrownBy(() -> service.getScheduleCalendar(scheduleId, yearMonth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no active version");
    }
}