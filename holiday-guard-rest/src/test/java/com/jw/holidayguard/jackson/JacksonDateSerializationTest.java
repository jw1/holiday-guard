package com.jw.holidayguard.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jw.holidayguard.controller.ControllerTestConfiguration;
import com.jw.holidayguard.controller.ManagementControllerTestBase;
import com.jw.holidayguard.controller.ShouldRunController;
import com.jw.holidayguard.service.ScheduleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Baseline test: verifies Spring Boot's auto-configured ObjectMapper handles
 * Java time types correctly (LocalDate, Instant).
 *
 * <p>This test is a migration sentinel: if it fails after the Spring Boot 4 upgrade,
 * it means Jackson 3 auto-configuration is not registering JavaTimeModule correctly.
 *
 * <p>Key assertion: LocalDate MUST serialize as ISO-8601 string "2025-12-25",
 * NOT as a numeric array [2025, 12, 25] (which happens when JavaTimeModule is absent).
 */
@WebMvcTest(controllers = ShouldRunController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
class JacksonDateSerializationTest extends ManagementControllerTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleQueryService scheduleQueryService;

    // --- LocalDate ---

    @Test
    void localDate_shouldSerializeAsIsoString() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 25);

        // when
        String json = objectMapper.writeValueAsString(date);

        // then - must be "2025-12-25" (string), not [2025,12,25] (array)
        assertThat(json).isEqualTo("\"2025-12-25\"");
    }

    @Test
    void localDate_shouldRoundTripCorrectly() throws Exception {
        // given
        LocalDate original = LocalDate.of(2025, 12, 25);

        // when
        String json = objectMapper.writeValueAsString(original);
        LocalDate deserialized = objectMapper.readValue(json, LocalDate.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void localDate_serializedJsonShouldNotBeNumericArray() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 1, 15);

        // when
        String json = objectMapper.writeValueAsString(date);

        // then - a numeric array [2025,1,15] means JavaTimeModule is missing
        assertThat(json)
            .as("LocalDate should serialize as ISO string, not a numeric array. " +
                "If this fails post-upgrade, JavaTimeModule is not registered.")
            .doesNotStartWith("[");
    }

    // --- Instant ---

    @Test
    void instant_shouldRoundTripCorrectly() throws Exception {
        // given
        Instant original = Instant.parse("2025-12-25T10:00:00Z");

        // when
        String json = objectMapper.writeValueAsString(original);
        Instant deserialized = objectMapper.readValue(json, Instant.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    // --- Module registration ---

    @Test
    void javaTimeModule_shouldBeRegistered() throws Exception {
        // Behavioral proof: if JavaTimeModule is absent, Jackson either throws
        // InvalidDefinitionException or serializes LocalDate as a numeric array.
        // Successful ISO-8601 string serialization means the module IS registered.
        LocalDate date = LocalDate.of(2025, 1, 15);

        // when
        String json = objectMapper.writeValueAsString(date);

        // then - "2025-01-15" confirms JavaTimeModule is registered and active
        assertThat(json)
            .as("JavaTimeModule must be registered on the auto-configured ObjectMapper. " +
                "If this fails post-upgrade, check Jackson 3 auto-configuration in Spring Boot 4.")
            .isEqualTo("\"2025-01-15\"");
    }
}
