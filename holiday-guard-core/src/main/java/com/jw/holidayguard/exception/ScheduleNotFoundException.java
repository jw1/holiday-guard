package com.jw.holidayguard.exception;

public class ScheduleNotFoundException extends RuntimeException {

    public ScheduleNotFoundException(String name) {
        super("Schedule not found with name: " + name);
    }

    public ScheduleNotFoundException(Long id) {
        super("Schedule not found with id: " + id);
    }
}