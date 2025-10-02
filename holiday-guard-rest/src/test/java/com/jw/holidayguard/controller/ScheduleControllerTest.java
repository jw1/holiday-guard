package com.jw.holidayguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.dto.request.CreateScheduleRequest;
import com.jw.holidayguard.dto.request.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = ScheduleController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import(com.jw.holidayguard.exception.GlobalExceptionHandler.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService service;

    @Test
    void createSchedule() throws Exception {
        var createRequest = CreateScheduleRequest.builder()
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        var savedSchedule = Schedule.builder()
                .id(null)
                .name("US Federal Holidays")
                .description("Standard US federal holidays")
                .build();

        when(service.createSchedule(any(CreateScheduleRequest.class))).thenReturn(savedSchedule);

        mockMvc.perform(post("/api/v1/schedules")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("US Federal Holidays"));
    }

    @Test
    void createScheduleWithDuplicateName() throws Exception {
        var createRequest = CreateScheduleRequest.builder()
                .name("Existing Schedule")
                .build();

        when(service.createSchedule(any(CreateScheduleRequest.class)))
                .thenThrow(new DuplicateScheduleException("Existing Schedule"));

        mockMvc.perform(post("/api/v1/schedules")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateSchedule() throws Exception {
        Long scheduleId = 1L;
        var updateRequest = UpdateScheduleRequest.builder()
                .name("Updated Name")
                .build();

        var updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .name("Updated Name")
                .build();

        when(service.updateSchedule(eq(scheduleId), any(UpdateScheduleRequest.class))).thenReturn(updatedSchedule);

        mockMvc.perform(put("/api/v1/schedules/{id}", scheduleId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateScheduleNotFound() throws Exception {
        Long scheduleId = 1L;
        var updateRequest = UpdateScheduleRequest.builder().name("Updated Name").build();

        when(service.updateSchedule(eq(scheduleId), any(UpdateScheduleRequest.class)))
                .thenThrow(new ScheduleNotFoundException(scheduleId));

        mockMvc.perform(put("/api/v1/schedules/{id}", scheduleId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    // Other tests omitted for brevity
}