package com.jw.holidayguard.exception;

import java.util.UUID;

public class ScheduleNotFoundException extends RuntimeException {
    
    public ScheduleNotFoundException(String name) {
        super("Schedule not found with name: " + name);
    }
    
    public ScheduleNotFoundException(UUID id) {
        super("Schedule not found with id: " + id);
    }
}