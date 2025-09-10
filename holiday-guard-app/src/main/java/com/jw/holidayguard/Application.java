package com.jw.holidayguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.jw.holidayguard")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
