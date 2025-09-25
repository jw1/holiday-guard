package com.jw.holidayguard.controller;

import com.jw.holidayguard.dto.ScheduleQueryLogDto;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleQueryService scheduleQueryService;

    @Test
    void getAllLogs_shouldReturnLogs() throws Exception {
        // given
        UUID logId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        Instant now = Instant.now();

        List<ScheduleQueryLogDto> logs = List.of(
                new ScheduleQueryLogDto(
                        logId,
                        scheduleId,
                        "Test Schedule",
                        versionId,
                        LocalDate.now(),
                        true,
                        "Test Reason",
                        false,
                        "test-client",
                        now
                )
        );

        when(scheduleQueryService.findAllLogs()).thenReturn(logs);

        // when & then
        mockMvc.perform(get("/api/v1/audit-logs").with(user("user")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].logId").value(logId.toString()))
                .andExpect(jsonPath("$[0].scheduleId").value(scheduleId.toString()))
                .andExpect(jsonPath("$[0].scheduleName").value("Test Schedule"))
                .andExpect(jsonPath("$[0].versionId").value(versionId.toString()))
                .andExpect(jsonPath("$[0].shouldRunResult").value(true))
                .andExpect(jsonPath("$[0].reason").value("Test Reason"))
                .andExpect(jsonPath("$[0].clientIdentifier").value("test-client"))
                .andExpect(jsonPath("$[0].createdAt").value(now.toString()));
    }
}
