package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.CreateScheduleRequest;
import com.jw.holidayguard.dto.request.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.RuleRepository;
import com.jw.holidayguard.repository.VersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

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
                .id(UUID.randomUUID())
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        var savedVersion = Version.builder()
                .id(UUID.randomUUID())
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
        var scheduleId = UUID.randomUUID();
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
        var scheduleId = UUID.randomUUID();
        when(repository.findById(scheduleId)).thenReturn(Optional.empty());

        // when/then - exception thrown because it is not found
        assertThatThrownBy(() -> service.findScheduleById(scheduleId))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    @Test
    void updateSchedule_shouldCreateNewVersion_whenRuleIsAddedForTheFirstTime() {
        // given
        var scheduleId = UUID.randomUUID();
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
        var scheduleId = UUID.randomUUID();
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
        var scheduleId = UUID.randomUUID();
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
}