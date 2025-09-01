package com.jw.holidayguard.controller;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorResponse to ensure proper error response construction.
 */
class ErrorResponseTest {

    @Test
    void shouldCreateErrorResponseWithAllFields() {
        // given
        String errorCode = "VALIDATION_ERROR";
        String message = "Name is required";
        Instant timestamp = Instant.now();

        // when
        ErrorResponse response = new ErrorResponse(errorCode, message, timestamp);

        // then
        assertEquals(errorCode, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void shouldCreateErrorResponseUsingStaticFactory() {
        // given
        String errorCode = "INVALID_REQUEST";
        String message = "Schedule not found";
        Instant beforeCreation = Instant.now();

        // when
        ErrorResponse response = ErrorResponse.of(errorCode, message);

        // then
        assertEquals(errorCode, response.getError());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
        
        // Verify timestamp is recent (within last second)
        Instant afterCreation = Instant.now();
        assertTrue(response.getTimestamp().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(response.getTimestamp().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    void shouldHandleNullValues() {
        // when
        ErrorResponse response = ErrorResponse.of(null, null);

        // then
        assertNull(response.getError());
        assertNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldHandleEmptyStrings() {
        // given
        String errorCode = "";
        String message = "";

        // when
        ErrorResponse response = ErrorResponse.of(errorCode, message);

        // then
        assertEquals(errorCode, response.getError());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
    }
}