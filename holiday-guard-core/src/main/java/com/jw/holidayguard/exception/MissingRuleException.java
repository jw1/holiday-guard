package com.jw.holidayguard.exception;

import java.util.UUID;

public class MissingRuleException extends RuntimeException {

    public MissingRuleException(UUID scheduleId) {
        super("No active rule found for schedule with ID: " + scheduleId);
    }
}
