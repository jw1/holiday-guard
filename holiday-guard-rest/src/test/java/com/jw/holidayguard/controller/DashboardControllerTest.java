package com.jw.holidayguard.controller;

import com.jw.holidayguard.domain.RunStatus;
import com.jw.holidayguard.dto.view.ScheduleDashboardView;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;


import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ContextConfiguration;

@WebMvcTest(DashboardController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
class DashboardControllerTest extends ManagementControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleQueryService scheduleQueryService;

    @Test
    void getStatusToday_shouldReturnDailyStatusForAllActiveSchedules() throws Exception {
        // given
        Long scheduleId1 = 1L;
        Long scheduleId2 = 2L;
        List<ScheduleDashboardView> statuses = List.of(
                new ScheduleDashboardView(scheduleId1, "ACH File Generation", RunStatus.RUN, true, "Scheduled to run"),
                new ScheduleDashboardView(scheduleId2, "Daily Reporting", RunStatus.SKIP, false, "Not scheduled to run")
        );

        when(scheduleQueryService.getDailyRunStatusForAllActiveSchedules()).thenReturn(statuses);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/schedule-status").with(user("user")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].scheduleId").value(scheduleId1.toString()))
                .andExpect(jsonPath("$[0].scheduleName").value("ACH File Generation"))
                .andExpect(jsonPath("$[0].status").value("RUN"))
                .andExpect(jsonPath("$[0].shouldRun").value(true))
                .andExpect(jsonPath("$[0].reason").value("Scheduled to run"))
                .andExpect(jsonPath("$[1].scheduleId").value(scheduleId2.toString()))
                .andExpect(jsonPath("$[1].scheduleName").value("Daily Reporting"))
                .andExpect(jsonPath("$[1].status").value("SKIP"))
                .andExpect(jsonPath("$[1].shouldRun").value(false))
                .andExpect(jsonPath("$[1].reason").value("Not scheduled to run"));
    }

    @Test
    void getTotalSchedulesCount_shouldReturnTotalCount() throws Exception {
        // given
        when(scheduleQueryService.getTotalSchedulesCount()).thenReturn(123L);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/stats/total-schedules").with(user("user")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(123));
    }

    @Test
    void getActiveSchedulesCount_shouldReturnActiveCount() throws Exception {
        // given
        when(scheduleQueryService.getActiveSchedulesCount()).thenReturn(99L);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard/stats/active-schedules").with(user("user")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(99));
    }
}
