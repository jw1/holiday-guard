package com.jw.holidayguard.controller;

import com.jw.holidayguard.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Baseline security tests: verifies the login endpoint works and documents
 * the current PasswordEncoder implementation.
 *
 * <h3>Migration forcing function</h3>
 * {@link #passwordEncoder_baseline_documentCurrentEncoder()} asserts the class name of
 * the current {@link PasswordEncoder} bean. When Phase 2 of the Spring Boot 4 migration
 * replaces {@code NoOpPasswordEncoder} with {@code DelegatingPasswordEncoder}, this test
 * will fail — intentionally — as a reminder to update the assertion and verify that
 * login still works with the new encoder.
 *
 * <h3>Login endpoint</h3>
 * The app uses a custom {@code UsernamePasswordAuthenticationFilter} at {@code POST /api/login}
 * that accepts form parameters (not JSON). These tests verify the endpoint honours
 * valid credentials with 200 and rejects bad credentials with 401.
 */
@WebMvcTest(controllers = ScheduleController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import({com.jw.holidayguard.security.SecurityConfig.class, com.jw.holidayguard.exception.GlobalExceptionHandler.class})
class SecurityAuthTest extends ManagementControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ScheduleService scheduleService;

    @Test
    void login_withValidUserCredentials_shouldReturn200() throws Exception {
        // given - valid in-memory credentials (defined in SecurityConfig.users())
        // when / then
        mockMvc.perform(post("/api/login")
                .param("username", "user")
                .param("password", "user"))
            .andExpect(status().isOk());
    }

    @Test
    void login_withValidAdminCredentials_shouldReturn200() throws Exception {
        // given - valid admin credentials
        // when / then
        mockMvc.perform(post("/api/login")
                .param("username", "admin")
                .param("password", "admin"))
            .andExpect(status().isOk());
    }

    @Test
    void login_withWrongPassword_shouldReturn401() throws Exception {
        // given - wrong password
        // when / then
        mockMvc.perform(post("/api/login")
                .param("username", "user")
                .param("password", "wrongpassword"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withUnknownUser_shouldReturn401() throws Exception {
        // given - user that does not exist
        // when / then
        mockMvc.perform(post("/api/login")
                .param("username", "nobody")
                .param("password", "anything"))
            .andExpect(status().isUnauthorized());
    }

    /**
     * Documents the current PasswordEncoder implementation.
     *
     * <p>This test is a sentinel: if the encoder changes (e.g. to BCrypt for production),
     * this fails intentionally — update the assertion and verify login tests still pass.
     */
    @Test
    void passwordEncoder_baseline_documentCurrentEncoder() {
        assertThat(passwordEncoder.getClass().getSimpleName())
            .as("PasswordEncoder is DelegatingPasswordEncoder with {noop} prefix for dev passwords. " +
                "Update this assertion if switching to a hashing encoder for production.")
            .isEqualTo("DelegatingPasswordEncoder");
    }
}
