package com.jw.holidayguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.dto.CreateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService service;

    @Test
    void createSchedule() throws Exception {
        
        // given - valid request, "saved" object backend returns
        var createRequest = new CreateScheduleRequest();
        createRequest.setName("US Federal Holidays");
        createRequest.setDescription("Standard US federal holidays");
                
        var savedSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        when(service.createSchedule(any(Schedule.class))).thenReturn(savedSchedule);

        // when - POST request is made
        // then - 201 Created with schedule data
        mockMvc.perform(post("/api/v1/schedules")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedSchedule.getId().toString()))
                .andExpect(jsonPath("$.name").value("US Federal Holidays"))
                .andExpect(jsonPath("$.description").value("Standard US federal holidays"));
    }

    @Test
    void createScheduleWithDuplicateName() throws Exception {
        
        // given - service throws duplicate exception
        var scheduleData = Schedule.builder()
                .name("Existing Schedule")
                .description("Some description")
                .build();

        when(service.createSchedule(any(Schedule.class)))
                .thenThrow(new DuplicateScheduleException("Existing Schedule"));

        // when - POST request is made
        // then - 409 Conflict
        mockMvc.perform(post("/api/v1/schedules")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scheduleData)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("DUPLICATE_SCHEDULE"))
                .andExpect(jsonPath("$.message").value("Schedule already exists with name: Existing Schedule"));
    }

    @Test
    void getScheduleById() throws Exception {
        
        // given - schedule exists in service
        var scheduleId = UUID.randomUUID();
        var existingSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .description("Test description")
                .build();

        when(service.findScheduleById(scheduleId)).thenReturn(existingSchedule);

        // when - GET request is made
        // then - 200 OK with schedule data
        mockMvc.perform(get("/api/v1/schedules/{id}", scheduleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                .andExpect(jsonPath("$.name").value("Test Schedule"))
                .andExpect(jsonPath("$.description").value("Test description"));
    }

    @Test
    void getScheduleByIdNotFound() throws Exception {
        
        // given - schedule does not exist
        var scheduleId = UUID.randomUUID();
        when(service.findScheduleById(scheduleId))
                .thenThrow(new ScheduleNotFoundException(scheduleId));

        // when - GET request is made  
        // then - 404 Not Found
        mockMvc.perform(get("/api/v1/schedules/{id}", scheduleId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("SCHEDULE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Schedule not found with id: " + scheduleId));
    }

    @Test
    void getAllActiveSchedules() throws Exception {
        
        // given - multiple active schedules exist
        var now = Instant.now();
        var activeSchedules = List.of(
                Schedule.builder()
                        .id(UUID.randomUUID())
                        .name("Schedule 1")
                        .country("US")
                        .active(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                Schedule.builder()
                        .id(UUID.randomUUID())
                        .name("Schedule 2")
                        .country("US")
                        .active(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        when(service.getAllActiveSchedules()).thenReturn(activeSchedules);

        // when - GET request is made
        // then - 200 OK with schedule list
        mockMvc.perform(get("/api/v1/schedules"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Schedule 1"))
                .andExpect(jsonPath("$[1].name").value("Schedule 2"));
    }

    @Test
    void updateSchedule() throws Exception {
        
        // given - schedule exists and update data
        var scheduleId = UUID.randomUUID();
        var updateData = Schedule.builder()
                .name("Updated Schedule")
                .description("Updated description")
                .build();
                
        var updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Updated Schedule")
                .description("Updated description")
                .build();

        when(service.updateSchedule(eq(scheduleId), any(Schedule.class))).thenReturn(updatedSchedule);

        // when - PUT request is made
        // then - 200 OK with updated schedule
        mockMvc.perform(put("/api/v1/schedules/{id}", scheduleId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Schedule"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateScheduleNotFound() throws Exception {
        
        // given - schedule does not exist
        var scheduleId = UUID.randomUUID();
        var updateData = Schedule.builder()
                .name("Updated Schedule")
                .build();

        when(service.updateSchedule(eq(scheduleId), any(Schedule.class)))
                .thenThrow(new ScheduleNotFoundException(scheduleId));

        // when - PUT request is made
        // then - 404 Not Found
        mockMvc.perform(put("/api/v1/schedules/{id}", scheduleId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("SCHEDULE_NOT_FOUND"));
    }

    @Test
    void deleteSchedule() throws Exception {
        
        // given - schedule exists
        var scheduleId = UUID.randomUUID();
        var deactivatedSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Test Schedule")
                .active(false)
                .build();

        when(service.deactivateSchedule(scheduleId)).thenReturn(deactivatedSchedule);

        // when - DELETE request is made
        // then - 204 No Content
        mockMvc.perform(delete("/api/v1/schedules/{id}", scheduleId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteScheduleNotFound() throws Exception {
        
        // given - schedule does not exist
        var scheduleId = UUID.randomUUID();
        when(service.deactivateSchedule(scheduleId))
                .thenThrow(new ScheduleNotFoundException(scheduleId));

        // when - DELETE request is made
        // then - 404 Not Found  
        mockMvc.perform(delete("/api/v1/schedules/{id}", scheduleId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("SCHEDULE_NOT_FOUND"));
    }
}