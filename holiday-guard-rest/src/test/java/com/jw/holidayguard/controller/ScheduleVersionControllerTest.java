package com.jw.holidayguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.ScheduleRule;
import com.jw.holidayguard.domain.ScheduleVersion;
import com.jw.holidayguard.dto.CreateScheduleRuleRequest;
import com.jw.holidayguard.dto.UpdateScheduleRuleRequest;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = ScheduleVersionController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import(GlobalExceptionHandler.class)
class ScheduleVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleVersionService service;


    @Test
    void shouldUpdateScheduleRuleSuccessfully() throws Exception {

        // given
        UUID scheduleId = UUID.randomUUID();
        UUID newVersionId = UUID.randomUUID();

        UpdateScheduleRuleRequest request = new UpdateScheduleRuleRequest();
        request.setEffectiveFrom(Instant.parse("2024-01-01T00:00:00Z"));
        request.setRule(
            new CreateScheduleRuleRequest(
                ScheduleRule.RuleType.WEEKDAYS_ONLY,
                null,
                LocalDate.of(2024, 1, 1),
                true
            )
        );

        ScheduleVersion newVersion = ScheduleVersion.builder()
            .id(newVersionId)
            .scheduleId(scheduleId)
            .effectiveFrom(Instant.parse("2024-01-01T00:00:00Z"))
            .active(true)
            .build();

        when(service.updateScheduleRule(eq(scheduleId), any(UpdateScheduleRuleRequest.class)))
            .thenReturn(newVersion);

        // when & then
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/versions", scheduleId)
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
        UUID scheduleId = UUID.randomUUID();

        UpdateScheduleRuleRequest invalidRequest = new UpdateScheduleRuleRequest();
        invalidRequest.setRule(null); // Null rule - should be invalid

        // when & then
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/versions", scheduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

}
