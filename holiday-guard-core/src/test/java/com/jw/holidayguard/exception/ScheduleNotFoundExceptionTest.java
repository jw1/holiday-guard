package com.jw.holidayguard.exception;

import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScheduleNotFoundException to ensure proper exception construction.
 */
class ScheduleNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithScheduleName() {
        // given
        String scheduleName = "Non-existent Schedule";

        // when
        ScheduleNotFoundException exception = new ScheduleNotFoundException(scheduleName);

        // then
        assertEquals("Schedule not found with name: " + scheduleName, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithScheduleId() {
        // given
        Long scheduleId = null;

        // when
        ScheduleNotFoundException exception = new ScheduleNotFoundException(scheduleId);

        // then
        assertEquals("Schedule not found with id: " + scheduleId, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleNullScheduleName() {
        // when
        ScheduleNotFoundException exception = new ScheduleNotFoundException((String) null);

        // then
        assertEquals("Schedule not found with name: null", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleNullScheduleId() {
        // when
        ScheduleNotFoundException exception = new ScheduleNotFoundException((Long) null);

        // then
        assertEquals("Schedule not found with id: null", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleEmptyScheduleName() {
        // given
        String scheduleName = "";

        // when
        ScheduleNotFoundException exception = new ScheduleNotFoundException(scheduleName);

        // then
        assertEquals("Schedule not found with name: ", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        // given
        ScheduleNotFoundException exception = new ScheduleNotFoundException("test");

        // then
        assertTrue(exception instanceof RuntimeException);
    }
}