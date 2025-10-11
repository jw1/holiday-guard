package com.jw.holidayguard.dto.view;

import com.jw.holidayguard.domain.RunStatus;

import java.time.LocalDate;

/**
 * Atomic unit representing status for a single day.
 *
 * <p>This is the building block for larger calendar views. It contains
 * just the date, run status, and optional reason - no schedule context.
 * Schedule information is provided by the parent view that contains this.
 */
public record DayStatusView(
    LocalDate date,
    RunStatus status,
    String reason  // deviation reason, or null if no deviation
) {}
