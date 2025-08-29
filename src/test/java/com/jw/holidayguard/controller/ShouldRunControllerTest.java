package com.jw.holidayguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.dto.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.ShouldRunQueryResponse;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShouldRunController.class)
class ShouldRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleQueryService service;

    @Test
    void shouldRunTodayWithClientQueryParam() throws Exception {

        // given - request details and a canned response from service
        UUID scheduleId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        ShouldRunQueryResponse response = new ShouldRunQueryResponse(
            scheduleId,
            today,
            true,
            "Scheduled to run - found in materialized calendar",
            false,
            versionId
        );

        // when - service is called a response is returned
        when(service.shouldRunToday(eq(scheduleId), any(ShouldRunQueryRequest.class)))
            .thenReturn(response);

        // then - controller handles request & response as expected
        mockMvc.perform(get("/api/v1/schedules/{scheduleId}/should-run", scheduleId)
                .param("client", "payroll-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(scheduleId.toString()))
                .andExpect(jsonPath("$.queryDate").value(today.toString()))
                .andExpect(jsonPath("$.shouldRun").value(true))
                .andExpect(jsonPath("$.reason").value("Scheduled to run - found in materialized calendar"))
                .andExpect(jsonPath("$.overrideApplied").value(false));
    }

    @Test
    void shouldRunTodayWithoutClientQueryParam() throws Exception {

        // given - request details and a canned response from service
        UUID scheduleId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        
        ShouldRunQueryResponse response = new ShouldRunQueryResponse(
            scheduleId,
            today,
            false,
            "Not scheduled to run - date not found in materialized calendar", 
            false,
            versionId
        );

        // when - service is called a response is returned
        when(service.shouldRunToday(eq(scheduleId), any(ShouldRunQueryRequest.class)))
            .thenReturn(response);

        // then - controller handles request & response as expected
        mockMvc.perform(get("/api/v1/schedules/{scheduleId}/should-run", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shouldRun").value(false));
    }

    @Test
    void shouldRunOnSpecificDateWithPostRequest() throws Exception {
        // Given: POST request with specific date
        UUID scheduleId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        LocalDate queryDate = LocalDate.of(2024, 3, 15);
        
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(queryDate, "report-generator");
        
        ShouldRunQueryResponse response = new ShouldRunQueryResponse(
            scheduleId,
            queryDate,
            true,
            "Scheduled to run - found in materialized calendar",
            false,
            versionId
        );

        when(service.shouldRunToday(eq(scheduleId), any(ShouldRunQueryRequest.class)))
            .thenReturn(response);

        // When & Then: POST request should succeed
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/should-run", scheduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDate").value("2024-03-15"))
                .andExpect(jsonPath("$.shouldRun").value(true));
    }

    @Test
    void shouldReturnBadRequestForInvalidPostRequest() throws Exception {
        // TODO:  if queryDate is missing, should assume "today"... client may simply want to provide clientIdentifier (let's default to being helpful)

        // Given: Invalid request (missing queryDate)
        UUID scheduleId = UUID.randomUUID();
        
        ShouldRunQueryRequest invalidRequest = new ShouldRunQueryRequest();
        invalidRequest.setClientIdentifier("test-client");
        // queryDate is null - should be invalid

        // When & Then: POST request should return bad request
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/should-run", scheduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // TODO:  should fail requests for "out of bounds" dates
}