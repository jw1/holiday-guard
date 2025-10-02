package com.jw.holidayguard.exception;

public class MissingRuleException extends RuntimeException {

    public MissingRuleException(Long scheduleId) {
        super("No active rule found for schedule with ID: " + scheduleId);
    }
}
