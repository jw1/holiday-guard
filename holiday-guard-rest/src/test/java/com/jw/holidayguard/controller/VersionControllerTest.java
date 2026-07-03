package com.jw.holidayguard.controller;

import tools.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.dto.request.CreateRuleRequest;
import com.jw.holidayguard.dto.request.UpdateRuleRequest;
import com.jw.holidayguard.dto.response.RuleResponse;
import com.jw.holidayguard.dto.response.VersionResponse;
import com.jw.holidayguard.exception.GlobalExceptionHandler;
import com.jw.holidayguard.service.ScheduleVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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

        // given - a schedule update request and canned service response
        Long scheduleId = 1L;
        Long newVersionId = 20L;

        UpdateRuleRequest request = new UpdateRuleRequest();
        request.setEffectiveFrom(Instant.parse("2024-01-01T00:00:00Z"));
        request.setRule(new CreateRuleRequest(
                Rule.RuleType.WEEKDAYS_ONLY,
                null,
                LocalDate.of(2024, 1, 1),
                true
        ));

        RuleResponse ruleResponse = new RuleResponse(
                100L, scheduleId, newVersionId,
                Rule.RuleType.WEEKDAYS_ONLY, null,
                LocalDate.of(2024, 1, 1),
                Instant.parse("2024-01-01T00:00:00Z"),
                true
        );

        VersionResponse versionResponse = new VersionResponse(
                newVersionId,
                scheduleId,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                true,
                List.of(ruleResponse),
                List.of()
        );

        when(service.updateScheduleRule(eq(scheduleId), any(UpdateRuleRequest.class)))
                .thenReturn(versionResponse);

        // when - posting a valid rule update
        // then - response includes version metadata and the new rule
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/versions", scheduleId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newVersionId.toString()))
                .andExpect(jsonPath("$.scheduleId").value(scheduleId.toString()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.rules[0].ruleType").value("WEEKDAYS_ONLY"))
                .andExpect(jsonPath("$.deviations").isArray());
    }

    @Test
    void shouldReturnBadRequestForInvalidScheduleRuleRequest() throws Exception {

        // given - an invalid request with null rule
        Long scheduleId = 1L;
        UpdateRuleRequest invalidRequest = new UpdateRuleRequest();
        invalidRequest.setRule(null);

        // when - posting an invalid rule update
        // then - 400 with error details
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/versions", scheduleId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists());
    }
}
