package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @InjectMocks
    private ScheduleService service;

    @Test
    void createSchedule() {
        
        // given - valid schedule data
        var scheduleData = Schedule.builder()
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();
        
        var savedSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        when(repository.findByName("US Federal Holidays")).thenReturn(Optional.empty());
        when(repository.save(any(Schedule.class))).thenReturn(savedSchedule);

        // when - schedule is created
        var result = service.createSchedule(scheduleData);

        // then - schedule is saved and returned
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("US Federal Holidays");
        assertThat(result.getId()).isNotNull();
        
        verify(repository).findByName("US Federal Holidays");
        verify(repository).save(scheduleData);
    }

    @Test
    void createScheduleWithDuplicateName() {
        
        // given - existing schedule with same name
        var existingSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .name("Existing Schedule")
                .build();
                
        var newSchedule = Schedule.builder()
                .name("Existing Schedule")
                .description("Different description")
                .build();

        when(repository.findByName("Existing Schedule")).thenReturn(Optional.of(existingSchedule));

        // when - attempting to create duplicate
        // then - exception is thrown
        assertThatThrownBy(() -> service.createSchedule(newSchedule))
                .isInstanceOf(DuplicateScheduleException.class)
                .hasMessageContaining("Existing Schedule");
                
        verify(repository).findByName("Existing Schedule");
        verify(repository, never()).save(any());
    }

    @Test
    void findScheduleById() {
        
        // given - schedule exists in repository
        var scheduleId = UUID.randomUUID();
        var existingSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));

        // when - schedule is retrieved by id
        var result = service.findScheduleById(scheduleId);

        // then - schedule is returned
        assertThat(result).isEqualTo(existingSchedule);
        verify(repository).findById(scheduleId);
    }

    @Test
    void findScheduleByIdNotFound() {
        
        // given - schedule does not exist
        var scheduleId = UUID.randomUUID();
        when(repository.findById(scheduleId)).thenReturn(Optional.empty());

        // when - attempting to find non-existent schedule
        // then - exception is thrown
        assertThatThrownBy(() -> service.findScheduleById(scheduleId))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining(scheduleId.toString());
                
        verify(repository).findById(scheduleId);
    }

    @Test
    void findScheduleByName() {
        
        // given - schedule exists in repository
        var existingSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .name("Bank Holidays")
                .build();

        when(repository.findByName("Bank Holidays")).thenReturn(Optional.of(existingSchedule));

        // when - schedule is retrieved by name
        var result = service.findScheduleByName("Bank Holidays");

        // then - schedule is returned
        assertThat(result).isEqualTo(existingSchedule);
        verify(repository).findByName("Bank Holidays");
    }

    @Test
    void findScheduleByNameNotFound() {
        
        // given - schedule does not exist
        when(repository.findByName("Missing Schedule")).thenReturn(Optional.empty());

        // when - attempting to find non-existent schedule
        // then - exception is thrown
        assertThatThrownBy(() -> service.findScheduleByName("Missing Schedule"))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining("Missing Schedule");
                
        verify(repository).findByName("Missing Schedule");
    }

    @Test
    void getAllActiveSchedules() {
        
        // given - multiple active schedules in repository
        var activeSchedules = List.of(
                Schedule.builder().name("Schedule 1").build(),
                Schedule.builder().name("Schedule 2").build()
        );

        when(repository.findByActiveTrue()).thenReturn(activeSchedules);

        // when - active schedules are retrieved
        var result = service.getAllActiveSchedules();

        // then - all active schedules are returned
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(activeSchedules);
        verify(repository).findByActiveTrue();
    }

    @Test
    void updateSchedule() {
        
        // given - existing schedule and update data
        var scheduleId = UUID.randomUUID();
        var existingSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Original Name")
                .description("Original description")
                .country("US")
                .build();
                
        var updateData = Schedule.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(repository.findByName("Updated Name")).thenReturn(Optional.empty());

        // when - schedule is updated
        var result = service.updateSchedule(scheduleId, updateData);

        // then - existing schedule is modified and returned
        assertThat(result).isSameAs(existingSchedule); // Same object reference
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getCountry()).isEqualTo("US"); // Unchanged
        
        verify(repository).findById(scheduleId);
        verify(repository).findByName("Updated Name");
        verify(repository, never()).save(any()); // JPA auto-saves managed entities
    }

    @Test
    void updateSchedulePartialUpdate() {
        
        // given - existing schedule and partial update data
        var scheduleId = UUID.randomUUID();
        var existingSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Original Name")
                .description("Original description")
                .country("US")
                .build();
                
        var updateData = Schedule.builder()
                .description("Only description updated")
                .build(); // Only description provided

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));

        // when - partial update is performed
        var result = service.updateSchedule(scheduleId, updateData);

        // then - only description is changed
        assertThat(result.getName()).isEqualTo("Original Name"); // Unchanged
        assertThat(result.getDescription()).isEqualTo("Only description updated");
        assertThat(result.getCountry()).isEqualTo("US"); // Unchanged
    }

    @Test
    void updateScheduleWithSameName() {
        
        // given - existing schedule and update with same name
        var scheduleId = UUID.randomUUID();
        var existingSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Same Name")
                .description("Original description")
                .build();
                
        var updateData = Schedule.builder()
                .name("Same Name") // Same as existing
                .description("Updated description")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));

        // when - update is performed with same name
        var result = service.updateSchedule(scheduleId, updateData);

        // then - no duplicate check is performed, update succeeds
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(repository).findById(scheduleId);
        verify(repository, never()).findByName(any()); // No duplicate check
    }

    @Test
    void updateScheduleWithDuplicateName() {
        
        // given - existing schedule and another schedule with same name
        var scheduleId = UUID.randomUUID();
        var existingSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Original Name")
                .build();
                
        var anotherSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .name("Existing Name")
                .build();
                
        var updateData = Schedule.builder()
                .name("Existing Name") // Conflicts with another schedule
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(repository.findByName("Existing Name")).thenReturn(Optional.of(anotherSchedule));

        // when - attempting to update with duplicate name
        // then - exception is thrown
        assertThatThrownBy(() -> service.updateSchedule(scheduleId, updateData))
                .isInstanceOf(DuplicateScheduleException.class)
                .hasMessageContaining("Existing Name");
                
        verify(repository).findById(scheduleId);
        verify(repository).findByName("Existing Name");
    }

    @Test
    void updateScheduleNotFound() {
        
        // given - schedule does not exist
        var scheduleId = UUID.randomUUID();
        var updateData = Schedule.builder()
                .name("Updated Name")
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.empty());

        // when - attempting to update non-existent schedule
        // then - exception is thrown
        assertThatThrownBy(() -> service.updateSchedule(scheduleId, updateData))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining(scheduleId.toString());
    }

    @Test
    void deactivateSchedule() {
        
        // given - active schedule exists
        var scheduleId = UUID.randomUUID();
        var activeSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Active Schedule")
                .active(true)
                .build();

        when(repository.findById(scheduleId)).thenReturn(Optional.of(activeSchedule));
        when(repository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when - schedule is deactivated
        var result = service.archiveSchedule(scheduleId, "test user");

        // then - schedule is marked inactive and saved
        assertThat(result.isActive()).isFalse();
        verify(repository).findById(scheduleId);
        verify(repository).save(activeSchedule);
    }
}