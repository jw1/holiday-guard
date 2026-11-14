package com.jw.holidayguard.controller;

import tools.jackson.databind.ObjectMapper;
import com.jw.holidayguard.domain.RunStatus;
import com.jw.holidayguard.dto.request.ShouldRunQueryRequest;
import com.jw.holidayguard.dto.response.ShouldRunQueryResponse;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Baseline test: verifies that LocalDate fields in REST request and response bodies
 * are serialized/deserialized as ISO-8601 strings ("2025-12-25") at the HTTP layer.
 *
 * <p>This is an end-to-end integration test through the full Spring MVC stack including
 * Jackson message converters. It catches regressions in Jackson date format handling
 * that unit tests of ObjectMapper alone might miss.
 *
 * <p>Post-migration check: if {@code $.queryDate} returns {@code [2025,12,25]} instead of
 * {@code "2025-12-25"}, Jackson 3's JavaTimeModule is not configured correctly.
 */
@WebMvcTest(controllers = ShouldRunController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import(com.jw.holidayguard.exception.GlobalExceptionHandler.class)
class DateSerializationIntegrationTest extends ManagementControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleQueryService service;

    @Test
    void post_shouldRunOnDate_deserializesLocalDateFromIsoString() throws Exception {
        // given - a request body with LocalDate as ISO string
        Long scheduleId = 1L;
        LocalDate queryDate = LocalDate.of(2025, 12, 25);
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(queryDate, "test-client");

        ShouldRunQueryResponse response = new ShouldRunQueryResponse(
            scheduleId, queryDate, true, RunStatus.RUN, "scheduled", false, 10L
        );
        when(service.shouldRunToday(eq(scheduleId), any(ShouldRunQueryRequest.class)))
            .thenReturn(response);

        // when - POST with {"queryDate": "2025-12-25", ...}
        mockMvc.perform(post("/api/v1/schedules/{id}/should-run", scheduleId)
                .with(user("user"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then - endpoint accepts it (date deserialized correctly)
            .andExpect(status().isOk());
    }

    @Test
    void response_dateField_shouldBeIsoString_notNumericArray() throws Exception {
        // given
        Long scheduleId = 1L;
        LocalDate queryDate = LocalDate.of(2025, 12, 25);

        ShouldRunQueryResponse response = new ShouldRunQueryResponse(
            scheduleId, queryDate, true, RunStatus.RUN, "scheduled", false, 10L
        );
        when(service.shouldRunToday(eq(scheduleId), any(ShouldRunQueryRequest.class)))
            .thenReturn(response);

        // when - GET request (date comes from response body)
        mockMvc.perform(get("/api/v1/schedules/{id}/should-run", scheduleId)
                .with(user("user")))
            // then - queryDate in response is ISO string, not [2025,12,25] array
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.queryDate").value("2025-12-25"));
    }

    @Test
    void serializedRequest_containsDateAsString() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 25);
        ShouldRunQueryRequest request = new ShouldRunQueryRequest(date, "test");

        // when - serialize to JSON
        String json = objectMapper.writeValueAsString(request);

        // then - date appears as ISO string, not array
        assertThat(json)
            .as("Serialized request must contain date as ISO string. " +
                "If this contains [2025,12,25], JavaTimeModule is not registered.")
            .contains("\"2025-12-25\"")
            .doesNotContain("[2025");
    }
}
