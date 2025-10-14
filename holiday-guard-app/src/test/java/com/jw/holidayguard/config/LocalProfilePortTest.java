package com.jw.holidayguard.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * Test to verify that the H2 profile uses port 8080 as configured.
 */
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles({"h2", "demo"})
@Slf4j
class LocalProfilePortTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldStartOnPort8080WithH2Profile() {
        assertThat(port).isEqualTo(8080);
        log.info("Application started on configured port (H2 profile): {}", port);
    }
}
