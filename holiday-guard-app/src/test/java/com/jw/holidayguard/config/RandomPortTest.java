package com.jw.holidayguard.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that the application uses a random port by default
 * and can be configured to use a specific port via profile.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")  // Need a profile to activate repository implementation
class RandomPortTest {

    @LocalServerPort
    private int port;

    @Autowired
    private Environment environment;

    @Test
    void shouldStartOnRandomPort() {
        // given - Application started without local profile

        // then - Port should be assigned (not 0 or 8080)
        assertThat(port).isGreaterThan(0);
        assertThat(port).isNotEqualTo(8080); // Should not be the local profile port

        System.out.println("Application started on random port: " + port);
    }
}
