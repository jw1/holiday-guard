package com.jw.holidayguard.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {

    private String error;
    private String message;
    private Instant timestamp;

    public ErrorResponse(String error, String message, Instant timestamp) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, Instant.now());
    }
}