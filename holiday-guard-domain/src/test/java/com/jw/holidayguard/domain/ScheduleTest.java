package com.jw.holidayguard.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleTest {

    static Validator validator;

    @BeforeAll
    static void initValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validScheduleWithDefaultedFields() {

        // given - schedule with required fields
        var schedule = Schedule.builder()
                .name("US Bank Holidays")
                .description("Standard US federal holidays")
                .build();

        // when - validator inspects
        var violations = validator.validate(schedule);

        // then - no violations found, fields set correctly
        assertThat(violations).isEmpty();
        assertThat(schedule.getName()).isEqualTo("US Bank Holidays");
        assertThat(schedule.getDescription()).isEqualTo("Standard US federal holidays");
        assertThat(schedule.getCountry()).isEqualTo("US");
        assertThat(schedule.isActive()).isTrue();
    }

    @Test
    void entityCanBeCreatedWithNullName() {

        // given - entity without name (validation now handled at DTO layer)
        var schedule = Schedule.builder().build();

        // when - validator inspects entity (no validation annotations)
        var violations = validator.validate(schedule);

        // then - no entity-level violations (validation moved to DTO layer)
        assertThat(violations).isEmpty();
        assertThat(schedule.getName()).isNull();
        assertThat(schedule.getCountry()).isEqualTo("US"); // default still works
        assertThat(schedule.isActive()).isTrue(); // default still works
    }

    @Test
    void entityCanBeCreatedWithNullCountry() {

        // given - entity with null country (validation now handled at DTO layer)  
        var schedule = Schedule.builder()
                .name("Test Schedule")
                .country(null)
                .build();

        // when - validator inspects entity (no validation annotations)
        var violations = validator.validate(schedule);

        // then - no entity-level violations (validation moved to DTO layer)
        assertThat(violations).isEmpty();
        assertThat(schedule.getName()).isEqualTo("Test Schedule");
        assertThat(schedule.getCountry()).isNull();
        assertThat(schedule.isActive()).isTrue(); // default still works
    }

    @Test
    void defaultedFieldsCanBeOverridden() {
        var schedule = Schedule.builder()
                .name("Canadian Holidays")
                .description("Canadian federal holidays")
                .country("CA")
                .active(false)
                .build();

        assertThat(schedule.getCountry()).isEqualTo("CA");
        assertThat(schedule.isActive()).isFalse();
    }
}