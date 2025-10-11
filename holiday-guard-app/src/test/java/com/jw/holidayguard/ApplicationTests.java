package com.jw.holidayguard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")  // Need a profile to activate repository implementation
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
