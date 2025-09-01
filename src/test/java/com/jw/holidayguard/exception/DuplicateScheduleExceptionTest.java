package com.jw.holidayguard.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DuplicateScheduleException to ensure proper exception construction.
 */
class DuplicateScheduleExceptionTest {

    @Test
    void shouldCreateExceptionWithScheduleName() {
        // given
        String scheduleName = "Payroll Schedule";

        // when
        DuplicateScheduleException exception = new DuplicateScheduleException(scheduleName);

        // then
        assertEquals("Schedule already exists with name: " + scheduleName, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleNullScheduleName() {
        // when
        DuplicateScheduleException exception = new DuplicateScheduleException(null);

        // then
        assertEquals("Schedule already exists with name: null", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleEmptyScheduleName() {
        // given
        String scheduleName = "";

        // when
        DuplicateScheduleException exception = new DuplicateScheduleException(scheduleName);

        // then
        assertEquals("Schedule already exists with name: ", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        // given
        DuplicateScheduleException exception = new DuplicateScheduleException("test");

        // then
        assertTrue(exception instanceof RuntimeException);
    }
}