package com.jw.holidayguard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI holidayGuardOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Holiday Guard API")
                        .description("REST service for managing business calendars, holidays, and schedule deviations. " +
                                "Query whether a job should run on a given date, manage schedule rules, and track overrides.")
                        .version("1.0.0")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
