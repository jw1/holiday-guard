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
    void shouldDefaultToTodayWhenQueryDateMissing() throws Exception {
        // Given: Request with missing queryDate should default to today
        UUID scheduleId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        
        ShouldRunQueryRequest request = new ShouldRunQueryRequest();
        request.setClientIdentifier("test-client");
        // queryDate is null - should default to today
        
        ShouldRunQueryResponse response = new ShouldRunQueryResponse(
            scheduleId,
            today,
            true,
            "Scheduled to run - found in materialized calendar",
            false,
            versionId
        );

        when(service.shouldRunToday(eq(scheduleId), any(ShouldRunQueryRequest.class)))
            .thenReturn(response);

        // When & Then: POST request should succeed and use today
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/should-run", scheduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDate").value(today.toString()))
                .andExpect(jsonPath("$.shouldRun").value(true));
    }

    @Test
    void shouldFailForOutOfBoundsDate() throws Exception {
        // Given: Request with date too far in future (beyond reasonable planning horizon)
        UUID scheduleId = UUID.randomUUID();
        LocalDate farFuture = LocalDate.now().plusYears(10); // 10 years in future
        
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(farFuture, "test-client");

        // Mock the service to throw IllegalArgumentException for out-of-bounds date
        when(service.shouldRunToday(eq(scheduleId), any(ShouldRunQueryRequest.class)))
                .thenThrow(new IllegalArgumentException("Query date too far in future: " + farFuture + " (max: " + LocalDate.now().plusYears(5) + ")"));

        // When & Then: POST request should return bad request for out of bounds date
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/should-run", scheduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }
}