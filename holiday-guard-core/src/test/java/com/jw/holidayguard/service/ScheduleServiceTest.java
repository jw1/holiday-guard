package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.CreateScheduleRequest;
import com.jw.holidayguard.dto.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleRuleRepository;
import com.jw.holidayguard.repository.ScheduleVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ScheduleRuleRepository ruleRepository;

    @Mock
    private ScheduleVersionRepository versionRepository;

    @InjectMocks
    private ScheduleService service;

    @Test
    void createSchedule() {
        // given
        var request = new CreateScheduleRequest();
        request.setName("US Federal Holidays");
        request.setDescription("Standard US federal holidays");
        request.setRuleType("WEEKDAYS_ONLY");
        request.setRuleConfig("");

        var savedSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        var savedVersion = ScheduleVersion.builder().id(UUID.randomUUID()).build();

        when(repository.findByName(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Schedule.class))).thenReturn(savedSchedule);
        when(versionRepository.save(any(ScheduleVersion.class))).thenReturn(savedVersion);

        // when
        var result = service.createSchedule(request);

        // then
        assertThat(result).isNotNull();
        verify(repository).save(any(Schedule.class));
        verify(versionRepository).save(any(ScheduleVersion.class));
        verify(ruleRepository).save(any(ScheduleRule.class));
    }

    @Test
    void createScheduleWithDuplicateName() {
        // given
        var request = new CreateScheduleRequest();
        request.setName("Existing Schedule");

        when(repository.findByName("Existing Schedule")).thenReturn(Optional.of(new Schedule()));

        // when/then
        assertThatThrownBy(() -> service.createSchedule(request))
                .isInstanceOf(DuplicateScheduleException.class);
    }

    @Test
    void findScheduleById() {
        // given
        var scheduleId = UUID.randomUUID();
        var existingSchedule = Schedule.builder().id(scheduleId).build();
        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));

        // when
        var result = service.findScheduleById(scheduleId);

        // then
        assertThat(result).isEqualTo(existingSchedule);
    }

    @Test
    void findScheduleByIdNotFound() {
        // given
        var scheduleId = UUID.randomUUID();
        when(repository.findById(scheduleId)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> service.findScheduleById(scheduleId))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    @Test
    void updateSchedule() {
        // given
        var scheduleId = UUID.randomUUID();
        var existingSchedule = new Schedule();
        existingSchedule.setName("Original Name");

        var request = UpdateScheduleRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .ruleType("CRON_EXPRESSION")
                .ruleConfig("* * * * *")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(repository.findByName("Updated Name")).thenReturn(Optional.empty());
        when(ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(scheduleId)).thenReturn(Optional.empty());
        when(versionRepository.save(any(ScheduleVersion.class))).thenReturn(new ScheduleVersion());

        // when
        var result = service.updateSchedule(scheduleId, request);

        // then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(versionRepository).save(any(ScheduleVersion.class));
        verify(ruleRepository).save(any(ScheduleRule.class));
    }

    // Other tests for findByName, getAllActiveSchedules, archiveSchedule etc. would go here
}