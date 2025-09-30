package com.jw.holidayguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.dto.request.CreateScheduleRequest;
import com.jw.holidayguard.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ScheduleController.class)
@Import({com.jw.holidayguard.security.SecurityConfig.class, com.jw.holidayguard.exception.GlobalExceptionHandler.class})
class ScheduleControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService service;

    @Test
    void getSchedules_withUserRole_shouldSucceed() throws Exception {
        when(service.findAllSchedules()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/schedules").with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getSchedules_withNoUser_shouldFail() throws Exception {
        mockMvc.perform(get("/api/v1/schedules"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSchedule_withAdminRole_shouldSucceed() throws Exception {
        var createRequest = CreateScheduleRequest.builder()
                .name("Test")
                .build();

        when(service.createSchedule(createRequest)).thenReturn(Schedule.builder().build());

        mockMvc.perform(post("/api/v1/schedules")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createSchedule_withUserRole_shouldFail() throws Exception {
        var createRequest = CreateScheduleRequest.builder()
                .name("Test")
                .build();

        // Add mock to prevent NPE, even though this should be blocked by security
        when(service.createSchedule(any(CreateScheduleRequest.class))).thenReturn(Schedule.builder().build());

        mockMvc.perform(post("/api/v1/schedules")
                .with(user("user").roles("USER"))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSchedule_withNoUser_shouldFail() throws Exception {
        var createRequest = CreateScheduleRequest.builder()
                .name("Test")
                .build();

        mockMvc.perform(post("/api/v1/schedules")
                .with(csrf()) // CSRF is checked before authentication
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }
}
