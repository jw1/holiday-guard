package com.jw.holidayguard.exception;

public class DuplicateScheduleException extends RuntimeException {
    
    public DuplicateScheduleException(String name) {
        super("Schedule already exists with name: " + name);
    }
}