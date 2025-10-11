package com.jw.holidayguard.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that the H2 profile uses port 8080 as configured.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("h2")
class LocalProfilePortTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldStartOnPort8080WithH2Profile() {
        // Given: Application started with H2 profile

        // Then: Port should be 8080 as configured in application-h2.yml
        assertThat(port).isEqualTo(8080);

        System.out.println("Application started on configured port (H2 profile): " + port);
    }
}
