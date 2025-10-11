package com.jw.holidayguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import com.jw.holidayguard.exception.GlobalExceptionHandler;
import com.jw.holidayguard.service.ScheduleVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = ScheduleVersionController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import(GlobalExceptionHandler.class)
class VersionControllerTest extends ManagementControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleVersionService service;


    @Test
    void shouldUpdateScheduleRuleSuccessfully() throws Exception {

        // given
        Long scheduleId = 1L;
        Long newVersionId = 20L;

        UpdateRuleRequest request = new UpdateRuleRequest();
        request.setEffectiveFrom(Instant.parse("2024-01-01T00:00:00Z"));
        request.setRule(
            new CreateRuleRequest(
                Rule.RuleType.WEEKDAYS_ONLY,
                null,
                LocalDate.of(2024, 1, 1),
                true
            )
        );

        Version newVersion = Version.builder()
            .id(newVersionId)
            .scheduleId(scheduleId)
            .effectiveFrom(Instant.parse("2024-01-01T00:00:00Z"))
            .active(true)
            .build();

        when(service.updateScheduleRule(eq(scheduleId), any(UpdateRuleRequest.class)))
            .thenReturn(newVersion);

        // when & then
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/versions", scheduleId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newVersionId.toString()))
                .andExpect(jsonPath("$.scheduleId").value(scheduleId.toString()))
                .andExpect(jsonPath("$.active").value(true));
    }


    @Test
    void shouldReturnBadRequestForInvalidScheduleRuleRequest() throws Exception {

        // given
        Long scheduleId = 1L;

        UpdateRuleRequest invalidRequest = new UpdateRuleRequest();
        invalidRequest.setRule(null); // Null rule - should be invalid

        // when & then
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/versions", scheduleId)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

}
